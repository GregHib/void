package content.entity.player.modal.tab

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.statement
import content.entity.sound.jingle
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random

class Emotes : Script {

    val definitions: InterfaceDefinitions by inject()

    companion object {
        val unlockableEmotes = listOf(
            "glass_box", "climb_rope", "lean", "glass_wall", "idea", "stomp", "flap", "slap_head", "zombie_walk", "zombie_dance",
            "zombie_hand", "scared", "bunny_hop", "snowman_dance", "air_guitar", "safety_first", "explore", "trick", "freeze", "give_thanks",
            "around_the_world_in_eggty_days", "dramatic_point", "faint", "puppet_master", "taskmaster", "seal_of_approval",
        )
    }

    init {
        interfaceOpen("emotes") { player ->
            for (compId in unlockableEmotes) {
                val component = definitions.getComponent(id, compId) ?: continue
                player.sendVariable("unlocked_emote_${component.stringId}")
            }
            player.sendVariable("unlocked_emote_lost_tribe")
        }

        interfaceRefresh("emotes") { player ->
            player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
        }

        interfaceOption(id = "emotes") {
            if (player.queue.contains("emote")) {
                return@interfaceOption
            }
            val id = option.toSnakeCase()
            val componentId = definitions.getComponent(this.id, component)!!
            if (componentId.index > 23 && !unlocked(id, option)) {
                return@interfaceOption
            }
            player.strongQueue("emote") {
                when {
                    id == "skillcape" -> {
                        val cape = player.equipped(EquipSlot.Cape)
                        val skill: Skill? = cape.def.getOrNull("skillcape_skill")
                        when {
                            cape.id == "quest_point_cape" -> playSkillCapeEmote(player, "quest_point")
                            cape.id == "dungeoneering_master_cape" -> playDungeoneeringMasterCapeEmote(player)
                            skill == Skill.Dungeoneering -> playDungeoneeringCapeEmote(player)
                            skill != null -> playSkillCapeEmote(player, skill.name.lowercase())
                        }
                    }
                    id == "seal_of_approval" -> playSealOfApprovalEmote(player)
                    id == "give_thanks" -> playGiveThanksEmote(player)
                    id == "angry" && player.equipped(EquipSlot.Hat).id == "a_powdered_wig" -> playEnhancedEmote(player, id)
                    id == "yawn" && player.equipped(EquipSlot.Hat).id == "sleeping_cap" -> playEnhancedYawnEmote(player)
                    id == "bow" && player.equipped(EquipSlot.Legs).id == "pantaloons" -> playEnhancedEmote(player, id)
                    id == "dance" && player.equipped(EquipSlot.Legs).id == "flared_trousers" -> playEnhancedEmote(player, id)
                    id == "flap" && player.equipped(EquipSlot.Feet).id == "chicken_feet" && player.equipped(EquipSlot.Legs).id == "chicken_legs" && player.equipped(EquipSlot.Chest).id == "chicken_wings" && player.equipped(EquipSlot.Hat).id == "chicken_head" -> playEnhancedEmote(player, id)
                    else -> {
                        if (id == "air_guitar") {
                            player.jingle(id)
                        }
                        player.gfx("emote_$id")
                        character.anim("emote_$id")
                    }
                }
            }
        }

        inventoryChanged("worn_equipment", EquipSlot.Cape) { player ->
            player["unlocked_emote_skillcape"] = item.def.contains("skill_cape") || item.def.contains("skill_cape_t") || item.id == "quest_point_cape"
        }
    }

    suspend fun SuspendableContext<Player>.unlocked(id: String, emote: String): Boolean {
        if (emote.startsWith("Goblin")) {
            if (player["unlocked_emote_lost_tribe", false]) {
                return true
            }
            statement("This emote can be unlocked during the Lost Tribe quest.")
            return false
        }
        if (emote == "Taskmaster") {
            if (player["task_progress_overall", 0] < 417) {
                statement("Complete the Task Master achievement to unlock this emote.")
                return false
            }
            if (!areaClear(player)) {
                return false
            }
        }
        if (!player["unlocked_emote_$id", false]) {
            when (emote) {
                "Glass Wall", "Glass Box", "Climb Rope", "Lean" -> statement("This emote can be unlocked during the mine random event.")
                "Zombie Dance", "Zombie Walk" -> statement("This emote can be unlocked during the gravedigger random event.")
                "Scared", "Trick", "Puppet master", "Zombie Hand" -> statement("This emote can be unlocked by playing a Halloween seasonal quest.")
                "Bunny Hop", "Around the World in Eggty Days" -> statement("This emote can be unlocked by playing an Easter seasonal event.")
                "Skillcape" -> player.message("You need to be wearing a skillcape in order to perform that emote.")
                "Air Guitar" -> player.message("You need to have 500 music tracks unlocked to perform that emote.")
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
                "Give Thanks" -> player.message("This emote can be unlocked by playing a Thanksgiving seasonal event.")
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
        if (emote == "Skillcape" && player.equipped(EquipSlot.Cape).id == "dungeoneering_master_cape" && !areaClear(player)) {
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

    suspend fun Interaction<Player>.playEnhancedEmote(player: Player, type: String) {
        player.animDelay("emote_enhanced_$type")
    }

    suspend fun Interaction<Player>.playEnhancedYawnEmote(player: Player) {
        player.gfx("emote_enhanced_yawn")
        player.animDelay("emote_enhanced_yawn")
    }

    suspend fun Interaction<Player>.playGiveThanksEmote(player: Player) {
        player.gfx("emote_give_thanks")
        player.animDelay("emote_turkey_transform")
        player.transform("turkey")
        player.animDelay("emote_turkey_dance")
        player.gfx("emote_give_thanks")
        player.clearTransform()
        player.animDelay("emote_turkey_return")
    }

    suspend fun Interaction<Player>.playSealOfApprovalEmote(player: Player) {
        player.gfx("emote_seal_of_approval")
        player.animDelay("emote_seal_of_approval")
        player.transform("seal")
        player.animDelay("emote_seal_clap")
        player.animDelay("emote_seal_return")
        player.gfx("emote_seal_of_approval")
        player.clearTransform()
        player.animDelay("emote_seal_stand")
    }

    suspend fun Interaction<Player>.playSkillCapeEmote(player: Player, skill: String) {
        player.gfx("emote_$skill")
        player.animDelay("emote_$skill")
    }

    suspend fun Interaction<Player>.playDungeoneeringCapeEmote(player: Player) {
        player.gfx("emote_dungeoneering_start")
        player.animDelay("emote_dungeoneering_start")
        when (random.nextInt(3)) {
            0 -> {
                player.transform("primal_warrior")
                player.animDelay("emote_dungeoneering_melee")
            }
            1 -> {
                player.transform("celestial_mage")
                player.animDelay("emote_dungeoneering_mage")
            }
            2 -> {
                player.transform("sagittarian_ranger")
                player.animDelay("emote_dungeoneering_range")
            }
        }
        player.clearTransform()
    }

    suspend fun Interaction<Player>.playDungeoneeringMasterCapeEmote(player: Player) {
        val direction = player.direction

        player.transform("sagittarian_ranger")
        player.gfx("emote_dung_master_bow")
        var tile = player.tile.add(direction.rotate(1))
        var rotation = tile.delta(player.tile).toDirection().rotate(2)
        areaGfx("emote_dung_master_hobgoblin", tile, rotation = rotation)
        player.animDelay("emote_dung_master_bow")

        player.transform("celestial_mage")
        player.gfx("emote_dung_master_spell")
        tile = player.tile.add(direction.rotate(7))
        rotation = tile.delta(player.tile).toDirection().rotate(4)
        areaGfx("emote_dung_master_gravecreeper", tile, rotation = rotation)
        player.animDelay("emote_dung_master_spell")

        player.transform("primal_warrior")
        player.gfx("emote_dung_master_return")
        tile = player.tile.add(direction)
        rotation = direction.inverse().rotate(7)
        areaGfx("emote_dung_master_flesh_spoiler", tile, rotation = rotation)
        tile = player.tile.add(direction.inverse())
        rotation = direction.rotate(3)
        areaGfx("emote_dung_master_cursebearer", tile, rotation = rotation)
        player.animDelay("emote_dung_master_sword")

        player.clearTransform()
    }
}
