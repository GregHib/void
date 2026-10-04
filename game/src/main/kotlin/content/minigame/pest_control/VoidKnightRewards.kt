package content.minigame.pest_control

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

/**
 * Spend Void Knight Commendation points earned in pest control on experience, void equipment or resource packs.
 * https://runescape.wiki/w/Void_Knight_Commendation_point
 */
class VoidKnightRewards : Script {

    init {
        npcOperate("Talk-to", KNIGHTS) {
            npc<Neutral>("Hello there, how can I help you?") // TODO proper message
            choice {
                option("I'd like to spend my commendation points.") {
                    rewards()
                }
                option<Neutral>("Nothing, thanks.")
            }
        }

        npcOperate("Exchange", KNIGHTS) {
            rewards()
        }
    }

    private suspend fun Player.rewards() {
        choice("You have ${get("pest_control_points", 0)} Void Knight Commendation points.") {
            option("Combat training") {
                training()
            }
            option("Void Knight equipment") {
                equipment()
            }
            option("Resource packs") {
                packs()
            }
            option("Nothing, thanks.")
        }
    }

    private suspend fun Player.training() {
        choice("Which skill would you like to train?") {
            skillOption(Skill.Attack)
            skillOption(Skill.Strength)
            skillOption(Skill.Defence)
            skillOption(Skill.Constitution)
            option("More...") {
                choice("Which skill would you like to train?") {
                    skillOption(Skill.Ranged)
                    skillOption(Skill.Magic)
                    skillOption(Skill.Prayer)
                }
            }
        }
    }

    private fun ChoiceOption.skillOption(skill: Skill) {
        option(skill.name) {
            choice("How many points would you like to spend?") {
                for (points in listOf(1, 10, 100)) {
                    option("$points ${if (points == 1) "point" else "points"}") {
                        train(skill, points)
                    }
                }
            }
        }
    }

    private fun Player.train(skill: Skill, points: Int) {
        val level = levels.getMax(skill)
        if (level < 25) {
            message("The Void Knights will not offer training in skills which you have a level of less than 25.")
            return
        }
        if (!spend(points)) {
            return
        }
        val experience = experience(skill, level, points)
        exp(skill, experience.toDouble())
        message("The Void Knight has granted you $experience ${skill.name} experience.")
    }

    private suspend fun Player.equipment() {
        val rows = Tables.get("pest_control_equipment").rows()
        choice("Which item would you like?") {
            for (row in rows.take(4)) {
                equipmentOption(row)
            }
            option("More...") {
                choice("Which item would you like?") {
                    for (row in rows.drop(4)) {
                        equipmentOption(row)
                    }
                }
            }
        }
    }

    private fun ChoiceOption.equipmentOption(row: RowDefinition) {
        val item = row.item("item")
        val points = row.int("points")
        option("${ItemDefinitions.get(item).name} ($points points)") {
            if (item != "void_seal_8_8" && !hasVoidRequirements()) {
                message("You need level 42 in Constitution, Attack, Defence, Strength, Ranged, Magic, and 22 Prayer to purchase that.")
                return@option
            }
            if (inventory.isFull()) {
                message("You don't have enough inventory space.")
                return@option
            }
            if (!spend(points)) {
                return@option
            }
            inventory.add(item)
            npc<Happy>("void_knight_pest_control", "Here you go, use it well.") // TODO proper message
        }
    }

    private suspend fun Player.packs() {
        choice("Which pack would you like?") {
            for (row in Tables.get("pest_control_packs").rows()) {
                val points = row.int("points")
                option("${row.rowId.replaceFirstChar { it.uppercase() }} pack ($points points)") {
                    buyPack(row)
                }
            }
        }
    }

    private fun Player.buyPack(row: RowDefinition) {
        val skill = Skill.valueOf(row.string("skill"))
        if (levels.getMax(skill) < 25) {
            message("You need level 25 ${skill.name} to purchase this pack.")
            return
        }
        if (inventory.spaces < row.itemList("items").size) {
            message("You don't have enough inventory space.")
            return
        }
        if (!spend(row.int("points"))) {
            return
        }
        val (min, max) = row.intList("item_amount")
        val (totalMin, totalMax) = row.intList("total")
        var left = random.nextInt(totalMin, totalMax + 1)
        for (item in row.itemList("items")) {
            val amount = random.nextInt(min, max + 1).coerceAtMost(left)
            if (amount < 1) {
                continue
            }
            inventory.add(item, amount)
            left -= amount
        }
    }

    private fun Player.spend(points: Int): Boolean {
        if (get("pest_control_points", 0) < points) {
            message("You don't have enough points.")
            return false
        }
        dec("pest_control_points", points)
        return true
    }

    private fun Player.hasVoidRequirements(): Boolean {
        for (skill in listOf(Skill.Constitution, Skill.Attack, Skill.Defence, Skill.Strength, Skill.Ranged, Skill.Magic)) {
            if (levels.getMax(skill) < 42) {
                return false
            }
        }
        return levels.getMax(Skill.Prayer) >= 22
    }

    companion object {
        private const val KNIGHTS = "void_knight_pest_control,void_knight_2_pest_control,void_knight_3_pest_control,void_knight_4_pest_control"

        /**
         * https://runescape.wiki/w/Void_Knight_Commendation_point#Experience
         */
        fun experience(skill: Skill, level: Int, points: Int): Int {
            val multiplier = when (skill) {
                Skill.Prayer -> 18
                Skill.Magic, Skill.Ranged -> 32
                else -> 35
            }
            val bonus = when {
                points >= 100 -> 1.1
                points >= 10 -> 1.01
                else -> 1.0
            }
            return (points * (level * level / 600) * multiplier * bonus).toInt()
        }
    }
}
