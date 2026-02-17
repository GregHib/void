package content.entity.player.modal.tab

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.statement
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random

class Emotes : Script {

    companion object {
        val unlockableEmotes = listOf(
            "glass_box", "climb_rope", "lean", "glass_wall", "idea", "stomp", "flap", "slap_head", "zombie_walk", "zombie_dance",
            "zombie_hand", "scared", "bunny_hop", "snowman_dance", "air_guitar", "safety_first", "explore", "trick", "freeze", "give_thanks",
            "around_the_world_in_eggty_days", "dramatic_point", "faint", "puppet_master", "taskmaster", "seal_of_approval",
        )
    }

    init {
        interfaceOpened("emotes") { id ->
            for (compId in unlockableEmotes) {
                val component = InterfaceDefinitions.getComponent(id, compId) ?: continue
                sendVariable("unlocked_emote_${component.stringId}")
            }
            sendVariable("unlocked_emote_lost_tribe")
        }

        interfaceRefresh("emotes") {
            interfaceOptions.unlockAll("emotes", "emotes", 0..190)
        }

        interfaceOption(id = "emotes:*") {
            if (queue.contains("emote")) {
                return@interfaceOption
            }
            val id = it.option.toSnakeCase()
            val componentId = InterfaceDefinitions.getComponent(it.id, it.component)!!
            if (componentId.index > 23 && !unlocked(id, it.option)) {
                return@interfaceOption
            }
            strongQueue("emote") {
                when {
                    id == "skillcape" -> {
                        val cape = equipped(EquipSlot.Cape)
                        val skill: Skill? = cape.def.getOrNull("skillcape_skill")
                        when {
                            cape.id == "quest_point_cape" -> playSkillCapeEmote("quest_point")
                            cape.id == "dungeoneering_master_cape" -> playDungeoneeringMasterCapeEmote()
                            skill == Skill.Dungeoneering -> playDungeoneeringCapeEmote()
                            skill != null -> playSkillCapeEmote(skill.name.lowercase())
                        }
                    }
                    id == "seal_of_approval" -> playSealOfApprovalEmote()
                    id == "give_thanks" -> playGiveThanksEmote()
                    id == "angry" && equipped(EquipSlot.Hat).id == "a_powdered_wig" -> playEnhancedEmote(id)
                    id == "yawn" && equipped(EquipSlot.Hat).id == "sleeping_cap" -> playEnhancedYawnEmote()
                    id == "bow" && equipped(EquipSlot.Legs).id == "pantaloons" -> playEnhancedEmote(id)
                    id == "dance" && equipped(EquipSlot.Legs).id == "flared_trousers" -> playEnhancedEmote(id)
                    id == "flap" && equipped(EquipSlot.Feet).id == "chicken_feet" && equipped(EquipSlot.Legs).id == "chicken_legs" && equipped(EquipSlot.Chest).id == "chicken_wings" && equipped(EquipSlot.Hat).id == "chicken_head" -> playEnhancedEmote(id)
                    else -> {
                        if (id == "air_guitar") {
                            jingle(id)
                        }
                        gfx("emote_$id")
                        anim("emote_$id")
                    }
                }
            }
        }

        slotChanged("worn_equipment", EquipSlot.Cape) {
            set("unlocked_emote_skillcape", it.item.def.contains("skill_cape") || it.item.def.contains("skill_cape_t") || it.item.id == "quest_point_cape")
        }
    }

    suspend fun Player.unlocked(id: String, emote: String): Boolean {
        if (emote.startsWith("Goblin")) {
            if (get("unlocked_emote_lost_tribe", false)) {
                return true
            }
            statement("This emote can be unlocked during the Lost Tribe quest.")
            return false
        }
        if (emote == "Taskmaster") {
            if (get("task_progress_overall", 0) < 417) {
                statement("Complete the Task Master achievement to unlock this emote.")
                return false
            }
            if (!areaClear(this)) {
                return false
            }
        }
        if (!get("unlocked_emote_$id", false)) {
            when (emote) {
                "Glass Wall", "Glass Box", "Climb Rope", "Lean" -> statement("This emote can be unlocked during the mine random event.")
                "Zombie Dance", "Zombie Walk" -> statement("This emote can be unlocked during the gravedigger random event.")
                "Scared", "Trick", "Puppet master", "Zombie Hand" -> statement("This emote can be unlocked by playing a Halloween seasonal quest.")
                "Bunny Hop", "Around the World in Eggty Days" -> statement("This emote can be unlocked by playing an Easter seasonal event.")
                "Skillcape" -> message("You need to be wearing a skillcape in order to perform that emote.")
                "Air Guitar" -> message("You need to have 500 music tracks unlocked to perform that emote.")
                "Safety First" -> {
                    statement(
                        """
                       You can't use this emote yet. Visit the Stronghold of Player Safety to
                       unlock it.
                    """,
                    )
                }
                "Explore" -> {
                    statement(
                        """
                        You can't use this emote yet. You will need to complete the Beginner
                        Tasks in the Lumbridge and Draynor Achievement Diary to use it.
                    """,
                    )
                }
                "Give Thanks" -> message("This emote can be unlocked by playing a Thanksgiving seasonal event.")
                "Snowman Dance", "Freeze", "Dramatic Point", "Seal of Approval" -> statement("This emote can be unlocked by playing a Christmas seasonal event.")
                "Flap", "Slap Head", "Idea", "Stomp" -> {
                    statement(
                        """
                        You can't use that emote yet. Visit the Stronghold of Security to
                        unlock it.
                    """,
                    )
                }
                "Faint" -> statement("This emote can be unlocked by completing the mime court case.")
            }
            return false
        }
        if (emote == "Skillcape" && equipped(EquipSlot.Cape).id == "dungeoneering_master_cape" && !areaClear(this)) {
            return false
        }
        return true
    }

    fun areaClear(player: Player): Boolean {
        Direction.all.forEach {
            if (player.blocked(it)) {
                player.message("You need a clear area to perform this emote.")
                return false
            }
        }
        return true
    }

    suspend fun Player.playEnhancedEmote(type: String) {
        animDelay("emote_enhanced_$type")
    }

    suspend fun Player.playEnhancedYawnEmote() {
        gfx("emote_enhanced_yawn")
        animDelay("emote_enhanced_yawn")
    }

    suspend fun Player.playGiveThanksEmote() {
        gfx("emote_give_thanks")
        animDelay("emote_turkey_transform")
        transform("turkey")
        animDelay("emote_turkey_dance")
        gfx("emote_give_thanks")
        clearTransform()
        animDelay("emote_turkey_return")
    }

    suspend fun Player.playSealOfApprovalEmote() {
        gfx("emote_seal_of_approval")
        animDelay("emote_seal_of_approval")
        transform("seal")
        animDelay("emote_seal_clap")
        animDelay("emote_seal_return")
        gfx("emote_seal_of_approval")
        clearTransform()
        animDelay("emote_seal_stand")
    }

    suspend fun Player.playSkillCapeEmote(skill: String) {
        gfx("emote_$skill")
        animDelay("emote_$skill")
    }

    suspend fun Player.playDungeoneeringCapeEmote() {
        gfx("emote_dungeoneering_start")
        animDelay("emote_dungeoneering_start")
        when (random.nextInt(3)) {
            0 -> {
                transform("primal_warrior")
                animDelay("emote_dungeoneering_melee")
            }
            1 -> {
                transform("celestial_mage")
                animDelay("emote_dungeoneering_mage")
            }
            2 -> {
                transform("sagittarian_ranger")
                animDelay("emote_dungeoneering_range")
            }
        }
        clearTransform()
    }

    suspend fun Player.playDungeoneeringMasterCapeEmote() {
        val direction = direction

        transform("sagittarian_ranger")
        gfx("emote_dung_master_bow")
        var tile = tile.add(direction.rotate(1))
        var rotation = tile.delta(tile).toDirection().rotate(2)
        areaGfx("emote_dung_master_hobgoblin", tile, rotation = rotation)
        animDelay("emote_dung_master_bow")

        transform("celestial_mage")
        gfx("emote_dung_master_spell")
        tile = tile.add(direction.rotate(7))
        rotation = tile.delta(tile).toDirection().rotate(4)
        areaGfx("emote_dung_master_gravecreeper", tile, rotation = rotation)
        animDelay("emote_dung_master_spell")

        transform("primal_warrior")
        gfx("emote_dung_master_return")
        tile = tile.add(direction)
        rotation = direction.inverse().rotate(7)
        areaGfx("emote_dung_master_flesh_spoiler", tile, rotation = rotation)
        tile = tile.add(direction.inverse())
        rotation = direction.rotate(3)
        areaGfx("emote_dung_master_cursebearer", tile, rotation = rotation)
        animDelay("emote_dung_master_sword")

        clearTransform()
    }
}
