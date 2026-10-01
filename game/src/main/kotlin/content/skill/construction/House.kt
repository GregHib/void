package content.skill.construction

import content.quest.clearInstance
import content.quest.exitInstance
import content.quest.instance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Zone

/**
 * Houses are a [HOUSE_SIZE] grid of zones, a [ROOM_GRID] area of rooms around the [START_ROOM] surrounded by a border.
 * Rooms are on [GROUND_LEVEL] with the dungeon on [DUNGEON_LEVEL]
 */
class House : Script {
    init {
        moved {
            if (!contains("house_owner")) {
                return@moved
            }
            val instance = instance()
            if (instance != null && Instances.owner(tile) == instance) {
                return@moved
            }
            leaveHouse(teleport = false)
        }

        playerDespawn {
            if (inOwnHouse()) {
                expelGuests()
            }
        }
    }

    companion object {
        const val ROOM_GRID = 7
        const val HOUSE_LEVELS = 4
        const val HOUSE_SIZE = ROOM_GRID + 2
        const val HOUSE_CENTRE = ROOM_GRID / 2
        const val DUNGEON_LEVEL = 0
        const val GROUND_LEVEL = 1
        const val START_ROOM = HOUSE_CENTRE + HOUSE_CENTRE * ROOM_GRID + GROUND_LEVEL * ROOM_GRID * ROOM_GRID

        val Player.houseRoomIds: List<String>
            get() = get("house_room_ids") ?: emptyList()

        val Player.houseRoomPositions: List<Int>
            get() = get("house_room_positions") ?: emptyList()

        val Player.houseRoomRotations: List<Int>
            get() = get("house_room_rotations") ?: emptyList()

        fun Player.addHouseRoom(id: String, position: Int, rotation: Int = 0) {
            set("house_room_ids", houseRoomIds + id)
            set("house_room_positions", houseRoomPositions + position)
            set("house_room_rotations", houseRoomRotations + rotation)
        }

        fun roomPosition(x: Int, y: Int, level: Int) = x + y * ROOM_GRID + level * ROOM_GRID * ROOM_GRID

        fun roomX(position: Int) = position % ROOM_GRID

        fun roomY(position: Int) = position / ROOM_GRID % ROOM_GRID

        fun roomLevel(position: Int) = position / (ROOM_GRID * ROOM_GRID)

        /**
         * The zone the room at [position] occupies in the house starting at [base]
         */
        fun roomZone(base: Zone, position: Int): Zone = base.add(roomX(position) + 1, roomY(position) + 1, roomLevel(position))

        /**
         * The room position of [zone] in the house starting at [base], or null if it's outside the room grid
         */
        fun roomPosition(base: Zone, zone: Zone): Int? {
            val x = zone.x - base.x - 1
            val y = zone.y - base.y - 1
            val level = zone.level - base.level
            if (x !in 0 until ROOM_GRID || y !in 0 until ROOM_GRID || level !in 0 until HOUSE_LEVELS) {
                return null
            }
            return roomPosition(x, y, level)
        }

        fun Player.inOwnHouse() = get<String>("house_owner") == accountName

        /**
         * South-west zone of the house the player is currently in
         */
        fun Player.houseBase(): Zone? {
            if (!contains("house_owner")) {
                return null
            }
            return instance()?.tile?.zone
        }

        /**
         * Filler for a room [position] without a room
         */
        fun emptyRoom(position: Int): Zone = emptySpace(roomX(position) + 1, roomY(position) + 1, roomLevel(position))

        /**
         * Filler for house grid spaces without a room
         */
        private fun emptySpace(x: Int, y: Int, level: Int): Zone {
            val space = when {
                level == DUNGEON_LEVEL -> "dungeon"
                x == 0 || y == 0 || x == HOUSE_SIZE - 1 || y == HOUSE_SIZE - 1 -> "border"
                else -> "land"
            }
            return Tables.tile("house_spaces.$space.template").zone
        }

        fun Player.loadHouse(base: Zone, buildMode: Boolean) {
            val entries = mutableListOf<Triple<Zone, Zone, Int>>()
            val placed = mutableSetOf<Zone>()
            val ids = houseRoomIds
            val rotations = houseRoomRotations
            for ((index, position) in houseRoomPositions.withIndex()) {
                val zone = roomZone(base, position)
                placed.add(zone)
                entries.add(Triple(Tables.tile("house_rooms.${ids[index]}.template").zone, zone, rotations[index]))
            }
            for (level in DUNGEON_LEVEL..GROUND_LEVEL) {
                for (x in 0 until HOUSE_SIZE) {
                    for (y in 0 until HOUSE_SIZE) {
                        val zone = base.add(x, y, level)
                        if (zone !in placed) {
                            entries.add(Triple(emptySpace(x, y, level), zone, 0))
                        }
                    }
                }
            }
            get<DynamicZones>().copy(entries)
            if (buildMode) {
                return
            }
            for (zone in placed) {
                removeHotspots(zone)
            }
        }

        private fun removeHotspots(zone: Zone) {
            for (tile in zone.toCuboid()) {
                for (obj in GameObjects.at(tile)) {
                    if (obj.def.containsOption("Build")) {
                        obj.remove()
                    }
                }
            }
        }

        suspend fun Player.houseLoading() {
            open("house_loading")
            delay(3)
            close("house_loading")
        }

        fun Player.leaveHouse(teleport: Boolean = true) {
            val owner: String = remove("house_owner") ?: return
            if (hasOpen("house_options")) {
                open("options")
            }
            if (owner == accountName) {
                expelGuests()
            }
            if (teleport) {
                exitInstance()
            } else {
                clearInstance()
            }
        }

        fun Player.expelGuests() {
            for (player in Players) {
                if (player != this && player.get<String>("house_owner") == accountName) {
                    player.leaveHouse()
                }
            }
        }
    }
}
