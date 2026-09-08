package content.minigame.mage_training_arena

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
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
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * Entrance hall, portals and the rules shared by every room of the Mage Training Arena.
 */
class MageTrainingArena : Script {

    init {
        objectOperate("Enter", "doorway_mage_training_arena") {
            if (levels.get(Skill.Magic) < 7) {
                statement("You need a Magic level of at least 7 to enter the guild.")
                return@objectOperate
            }
            val direction = if (tile.y < 3300) Direction.NORTH else Direction.SOUTH
            sound("mta_barrier")
            anim("pass_through_barrier")
            exactMoveDelay(tile.add(direction.delta.x * 2, direction.delta.y * 2), delay = 90, direction = direction, startDelay = 30)
        }

        objectOperate("Enter", "telekinetic_portal,alchemists_portal,enchanters_portal,graveyard_portal") { (target) ->
            val room = rooms.firstOrNull { it.obj("portal") == target.id } ?: return@objectOperate
            enterRoom(room)
        }

        objectOperate("Enter", "exit_portal_mage_training_arena") {
            val room = currentRoom(this) ?: return@objectOperate
            tele(room.tile("exit"))
        }

        for (room in rooms) {
            entered(room.string("area")) {
                set("mage_training_arena_room", room.rowId)
                open(room.string("overlay"))
                PizazzPoints.refresh(this, room.rowId)
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

        npcOperate("Talk-to", "charmed_warrior") {
            player<Neutral>("Is there anybody there?")
            npc<Neutral>("What do you think?")
        }

        npcOperate("Talk-to", "charmed_warrior_2") {
            player<Neutral>("Is there anybody there?")
            npc<Neutral>("Wooo wooo! Be afraid for I'm a scary ghost. Wooo!")
            player<Neutral>("Er, whatever.")
        }

        npcOperate("Talk-to", "charmed_warrior_3") {
            player<Neutral>("Hello?")
            npc<Neutral>("Hey! You haven't paid for your Magic Training Arena Membership money!")
            player<Neutral>("You're lying. I can see right through you!")
            npc<Neutral>("Oh, HA HA, very funny.")
        }

        npcOperate("Talk-to", "charmed_warrior_4") {
            player<Neutral>("Hello?")
            npc<Neutral>("Can't you see I'm busy?")
            player<Neutral>("Well, I can't really see YOU.")
        }
    }

    private suspend fun Player.enterRoom(room: RowDefinition) {
        if (!get("mage_training_arena_started", false) || !hasProgressHat(this)) {
            statement("You need a Pizazz Progress Hat in order to enter. Talk to the Entrance Guardian if you don't have one.")
            return
        }
        if (follower != null) {
            statement("You can't take a familiar into the arena.")
            return
        }
        if (levels.get(Skill.Magic) < room.int("level")) {
            statement("You need to be able to cast the ${room.string("spell")} spell in order to enter.")
            return
        }
        when (room.rowId) {
            "alchemist" -> if (inventory.contains("coins")) {
                statement("You cannot take money into the Alchemists' Playground.")
                return
            }
            "graveyard" -> if (inventory.contains("banana") || inventory.contains("peach")) {
                statement("You can't take bananas or peaches into the arena.")
                return
            }
        }
        if (room.rowId == "telekinetic") {
            TelekineticTheatre.start(this)
        } else {
            tele(room.tile("enter"))
        }
        message("You've entered the ${room.string("name")}.")
    }

    companion object {
        val lobby = Tile(3363, 3302, 0)

        val rooms: List<RowDefinition>
            get() = Tables.get("mta_rooms").rows()

        /**
         * Items that belong to the arena and are removed when a player leaves a room by any means.
         */
        private val arenaItems = listOf(
            "coins_mage_training_arena",
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

        fun currentRoom(player: Player): RowDefinition? {
            val room: String = player.get("mage_training_arena_room") ?: return null
            return Rows.getOrNull("mta_rooms.$room")
        }

        fun inRoom(player: Player, room: String): Boolean = player["mage_training_arena_room", ""] == room

        fun hasProgressHat(player: Player): Boolean = PizazzHat.hats.any { player.inventory.contains(it) || player.equipment.contains(it) }

        fun confiscate(player: Player) {
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
    }
}

/**
 * Pizazz points are stored in four varbits read by the rewards interface and shown on each room's overlay.
 */
object PizazzPoints {
    val rooms = listOf("telekinetic", "alchemist", "enchanting", "graveyard")

    private val limits = mapOf(
        "telekinetic" to 8191,
        "alchemist" to 8191,
        "enchanting" to 32767,
        "graveyard" to 16383,
    )

    fun key(room: String) = "mage_training_arena_${room}_points"

    fun get(player: Player, room: String): Int = player[key(room), 0]

    fun total(player: Player): Int = rooms.sumOf { get(player, it) }

    fun add(player: Player, room: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        player.inc(key(room), amount, max = limits.getValue(room))
        refresh(player, room)
        PizazzHat.refresh(player)
    }

    fun remove(player: Player, room: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        player.dec(key(room), amount, min = 0)
        refresh(player, room)
    }

    fun reset(player: Player) {
        for (room in rooms) {
            player.set(key(room), 0)
            refresh(player, room)
        }
    }

    fun refresh(player: Player, room: String) {
        val overlay = Rows.getOrNull("mta_rooms.$room")?.string("overlay") ?: return
        if (!player.hasOpen(overlay)) {
            return
        }
        player.interfaces.sendText(overlay, "points", get(player, room).toString())
    }
}
