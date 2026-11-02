package content.minigame.mage_training_arena

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

/**
 * Entrance hall, portals and the rules shared by every room of the Mage Training Arena.
 */
class MageTrainingArena : Script {

    /**
     * Items that belong to the arena and are removed when a player leaves a room by any means.
     */
    private val arenaItems = listOf(
        "leather_boots_mage_training_arena",
        "adamant_kiteshield_mage_training_arena",
        "adamant_helm_mage_training_arena",
        "emerald_mage_training_arena",
        "rune_longsword_mage_training_arena",
        "cube",
        "cylinder",
        "icosahedron",
        "pentamid",
        "dragonstone_mage_training_arena",
        "orb",
        "animals_bones_1",
        "animals_bones_2",
        "animals_bones_3",
        "animals_bones_4",
        "banana",
        "peach",
    )

    init {
        objectOperate("Enter", "doorway_mage_training_arena") { (target) ->
            target.anim("mta_light_door_open")
            sound("light_beam_start")
            walkOverDelay(target.tile.addY(if (tile.y < target.tile.y) 1 else -1))
        }

        objectOperate("Enter", "alchemists_portal,enchanters_portal,graveyard_portal") { (target) ->
            val room = Rows.getOrNull("mta_rooms.${target.id.removeSuffix("_portal")}") ?: return@objectOperate
            if (!canEnter(this, room)) {
                return@objectOperate
            }
            tele(room.tile("enter"))
        }

        objectOperate("Enter", "exit_portal_mage_training_arena") {
            val room = currentRoom(this) ?: return@objectOperate
            tele(room.tile("exit"))
        }

        for (room in Tables.get("mta_rooms").rows()) {
            entered(room.string("area")) {
                set("mage_training_arena_room", room.rowId)
                open(room.string("overlay"))
                PizazzPoints.refresh(this, room.rowId)
                message("You've entered the ${room.string("name")}.")
            }
            exited(room.string("area")) {
                close(room.string("overlay"))
                clear("mage_training_arena_room")
                confiscate(this)
            }
        }

        playerDeath {
            if (!contains("mage_training_arena_room")) {
                return@playerDeath
            }
            it.dropItems = false
            it.teleport = lobby
        }

        for (type in listOf("modern", "ancient", "lunar", "dungeoneering", "jewellery", "tablet", "scroll", "home_teleport", "ectophial", "skull_sceptre")) {
            teleportTakeOff(type) {
                if (!contains("mage_training_arena_room")) {
                    return@teleportTakeOff true
                }
                message("You can't teleport out of the training arena!")
                false
            }
        }

        adminCommand("pizazz", intArg("amount", desc = "Points to add, negative to remove"), stringArg("room", optional = true, autofill = Tables.get("mta_rooms").rows().map { it.rowId }.toSet() + "all"), desc = "Add Pizazz Points to one room or all four") { args ->
            val amount = args[0].toInt()
            val room = args.getOrNull(1) ?: "all"
            val rooms = if (room == "all") Tables.get("mta_rooms").rows().map { it.rowId } else listOf(room)
            for (name in rooms) {
                if (Rows.getOrNull("mta_rooms.${name}") != null) {
                    message("Unknown room '$name'; use ${Tables.get("mta_rooms").rows().joinToString { it.rowId }} or all.")
                    return@adminCommand
                }
                if (amount >= 0) {
                    PizazzPoints.add(this, name, amount)
                } else {
                    PizazzPoints.remove(this, name, -amount)
                }
            }
            message("Pizazz Points: ${PizazzHat.points(this)}")
        }

        npcOperate("Talk-to", "charmed_warrior") {
            player<Quiz>("Is there anybody there?")
            npc<Neutral>("What do you think?")
        }

        npcOperate("Talk-to", "charmed_warrior_2") {
            player<Quiz>("Is there anybody there?")
            npc<Neutral>("Wooo wooo! Be afraid for I'm a scary ghost. Wooo!")
            player<Neutral>("Er, whatever.")
        }

        npcOperate("Talk-to", "charmed_warrior_3") {
            player<Quiz>("Hello?")
            npc<Neutral>("Hey! You haven't paid for your Magic Training Arena Membership money!")
            player<Happy>("You're lying. I can see right through you!")
            npc<Neutral>("Oh, HA HA, very funny.")
        }

        npcOperate("Talk-to", "charmed_warrior_4") {
            player<Quiz>("Hello?")
            npc<Neutral>("Can't you see I'm busy?")
            player<Neutral>("Well, I can't really see YOU.")
        }
    }

    private fun currentRoom(player: Player): RowDefinition? {
        val room: String = player.get("mage_training_arena_room") ?: return null
        return Rows.getOrNull("mta_rooms.$room")
    }

    private fun confiscate(player: Player) {
        for (item in arenaItems) {
            val held = player.inventory.count(item)
            if (held > 0) {
                player.inventory.remove(item, held)
            }
            val worn = player.equipment.count(item)
            if (worn > 0) {
                player.equipment.remove(item, worn)
            }
        }
    }

    companion object {
        val lobby = Tile(3363, 3302, 0)

        fun inRoom(player: Player, room: String): Boolean = player["mage_training_arena_room", ""] == room

        fun hasProgressHat(player: Player): Boolean = PizazzHat.hats.any { player.inventory.contains(it) || player.equipment.contains(it) }

        /**
         * The rules every room's portal applies before letting a player in, telling them why if not.
         */
        suspend fun canEnter(player: Player, room: RowDefinition): Boolean {
            if (!player["mage_training_arena_started", false] || !hasProgressHat(player)) {
                player.statement("You need a Pizazz Progress Hat in order to enter. Talk to the Entrance Guardian if you don't have one.")
                return false
            }
            if (player.follower != null) {
                player.statement("You can't take a familiar into the arena.")
                return false
            }
            if (player.levels.get(Skill.Magic) < room.int("level")) {
                player.statement("You need to be able to cast the ${room.string("spell")} spell in order to enter.")
                return false
            }
            when (room.rowId) {
                "alchemist" -> if (player.inventory.contains("coins")) {
                    player.statement("You cannot take money into the Alchemists' Playground.")
                    return false
                }
                "graveyard" -> if (player.inventory.contains("banana") || player.inventory.contains("peach")) {
                    player.statement("You can't take bananas or peaches into the arena.")
                    return false
                }
            }
            return true
        }
    }
}
