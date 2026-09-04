package content.activity.ancient_effigies

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import content.social.assist.Assistance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.random

/**
 * Ancient effigies drop from monsters and are investigated through four stages, each demanding
 * knowledge of one of two random skills, before turning into a dragonkin lamp. Historian Minas at
 * the Varrock Museum exchanges unopened effigies for antique lamps.
 * https://runescape.wiki/w/Ancient_effigies
 */
class AncientEffigies : Script {

    init {
        itemOption("Investigate", "*_ancient_effigy") { (item, slot) ->
            investigate(item, slot)
        }

        itemOption("Rub", "dragonkin_lamp") { (item, slot) ->
            val skill = skillLamp()
            // Constitution is measured in tenths internally (level 10 = 100)
            val level = if (skill == Skill.Constitution) levels.getMax(skill) / 10 else levels.getMax(skill)
            val experience = dragonkinExperience(level)
            if (inventory.remove(slot, item.id)) {
                exp(skill, experience)
                item(item.id, "As you focus on your chosen memories, you feel a burning malevolence in the back of your mind. You have gained new insight into ${skill.name}...but at what cost?")
            }
        }

        itemOption("Rub", "antique_lamp_ancient_effigies") { (item, slot) ->
            val skill = skillLamp()
            val level = if (skill == Skill.Constitution) levels.getMax(skill) / 10 else levels.getMax(skill)
            if (level < 50) {
                statement("<red>This skill is not high enough to gain experience from this lamp.")
                return@itemOption
            }
            if (inventory.remove(slot, item.id)) {
                exp(skill, 5000.0)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded 5,000 ${skill.name} experience!")
            }
        }
    }

    private suspend fun Player.investigate(item: Item, slot: Int) {
        val stage = stages.getValue(item.id)
        val key = "effigy_${item.id.removeSuffix("_ancient_effigy")}"
        val pair = pairs[getOrPut(key) { random.nextInt(pairs.size) }]
        item(item.id, "As you inspect the ancient effigy you begin to feel a strange sensation of the relic searching your mind, drawing on your knowledge.")
        item(item.id, "Images from your experiences of ${flavours.getValue(pair.first)} fill your mind.")
        choice("Which images do you wish to focus on?") {
            option(pair.first.name) {
                focus(item, slot, stage, key, pair.first)
            }
            option(pair.second.name) {
                focus(item, slot, stage, key, pair.second)
            }
        }
    }

    private suspend fun Player.focus(item: Item, slot: Int, stage: Stage, key: String, skill: Skill) {
        val assistant = Assistance.assistant(this, skill)
        if (levels.get(skill) < stage.level && (assistant == null || assistant.levels.get(skill) < stage.level)) {
            anim("effigy_fail")
            item(item.id, "The images in your mind fade; the ancient effigy seems to desire knowledge of experiences you have not yet had.")
            message("You require at least level ${stage.level} ${skill.name} to investigate the ancient effigy further.")
            return
        }
        item(item.id, "As you focus on your memories, you can almost hear a voice in the back of your mind whispering to you...")
        if (!inventory.replace(slot, item.id, stage.next)) {
            return
        }
        clear(key)
        exp(skill, stage.xp)
        if (stage.next == "dragonkin_lamp") {
            anim("effigy_transform")
            gfx("effigy_transform")
        } else {
            anim("effigy_learn")
        }
        message("You have gained ${stage.xp.toInt().toDigitGroupString()} ${skill.name} experience!")
        item(stage.next, "The ancient effigy glows briefly; it seems changed somehow and no longer responds to the same memories as before.")
        item(stage.next, "A sudden bolt of inspiration flashes through your mind, revealing new insight into your experiences!")
    }

    private fun dragonkinExperience(level: Int): Double = if (level >= 30) {
        (level * level * level - 2.0 * level * level + 100.0 * level) / 20.0
    } else {
        Level.experience(level) - Level.experience(level - 1)
    }

    private data class Stage(val level: Int, val xp: Double, val next: String)

    companion object {
        private val stages = mapOf(
            "starved_ancient_effigy" to Stage(91, 15000.0, "nourished_ancient_effigy"),
            "nourished_ancient_effigy" to Stage(93, 20000.0, "sated_ancient_effigy"),
            "sated_ancient_effigy" to Stage(95, 25000.0, "gorged_ancient_effigy"),
            "gorged_ancient_effigy" to Stage(97, 30000.0, "dragonkin_lamp"),
        )

        private val pairs = listOf(
            Skill.Agility to Skill.Crafting,
            Skill.Construction to Skill.Thieving,
            Skill.Cooking to Skill.Firemaking,
            Skill.Fishing to Skill.Farming,
            Skill.Fletching to Skill.Woodcutting,
            Skill.Herblore to Skill.Hunter,
            Skill.Mining to Skill.Smithing,
            Skill.Summoning to Skill.Runecrafting,
        )

        private val flavours = mapOf(
            Skill.Agility to "deftness and precision",
            Skill.Construction to "buildings and security",
            Skill.Cooking to "fire and preparation",
            Skill.Fishing to "life and cultivation",
            Skill.Fletching to "lumber and woodworking",
            Skill.Herblore to "flora and fauna",
            Skill.Mining to "metalwork and minerals",
            Skill.Summoning to "binding essence and spirits",
        )
    }
}
