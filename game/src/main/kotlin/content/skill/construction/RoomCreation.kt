package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.DUNGEON_LEVEL
import content.skill.construction.House.Companion.HOUSE_CENTRE
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.exitPortals
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.loadHouse
import content.skill.construction.House.Companion.removeHouseRoom
import content.skill.construction.House.Companion.roomLevel
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class RoomCreation(val dynamicZones: DynamicZones) : Script {
    init {
        objectOperate("Build", "door_hotspot*") { (target) ->
            val base = houseBase()
            if (base == null || !inOwnHouse() || !get("house_build_mode", false)) {
                return@objectOperate
            }
            val zone = target.tile.zone
            // Doors lead to the room on the opposite side to the player
            val room = if (tile.zone == zone) zone.add(direction(target.tile)) else zone
            val position = roomPosition(base, room)
            val index = houseRoomPositions.indexOf(position ?: -1)
            if (index != -1) {
                removeRoom(houseRoomPositions[index], houseRoomIds[index])
                return@objectOperate
            }
            val range = buildRange()
            if (room.x - base.x - 1 !in range) {
                message("Your house is already at the maximum width.")
                return@objectOperate
            }
            if (room.y - base.y - 1 !in range) {
                message("Your house is already at the maximum height.")
                return@objectOperate
            }
            if (houseRoomIds.size >= maxRooms()) {
                message("You need a higher Construction level to build any more rooms.") // TODO proper messages
                return@objectOperate
            }
            set("house_preview_position", position ?: return@objectOperate)
            open("room_creation")
        }

        interfaceOption("Build", "room_creation:*") {
            close("room_creation")
            val position: Int = get("house_preview_position") ?: return@interfaceOption
            val row = Rows.getOrNull("house_rooms.${it.component}") ?: return@interfaceOption
            if (!has(Skill.Construction, row.int("level"), message = true)) {
                return@interfaceOption
            }
            val dungeon = row.bool("dungeon")
            if (dungeon && roomLevel(position) != DUNGEON_LEVEL) {
                message("That room can only be built underground.") // TODO proper messages
                return@interfaceOption
            }
            if (!dungeon && roomLevel(position) == DUNGEON_LEVEL) {
                message("That room can't be built underground.") // TODO proper messages
                return@interfaceOption
            }
            if (inventory.count("coins") < row.int("cost")) {
                message("You need ${row.int("cost")} coins to build this room.") // TODO proper messages
                return@interfaceOption
            }
            set("house_preview_room", it.component)
            set("house_preview_rotation", 0)
            walkTrigger { clearPreview() }
            preview()
        }
    }

    /**
     * Side of the room [tile] is a door on
     */
    private fun direction(tile: Tile): Direction {
        val zone = tile.zone.tile
        return when {
            tile.x == zone.x -> Direction.WEST
            tile.y == zone.y -> Direction.SOUTH
            tile.x == zone.x + 7 -> Direction.EAST
            else -> Direction.NORTH
        }
    }

    /**
     * Range of room grid coordinates the players level allows building in
     * Sizes grow from the south-west, so even sizes extend one further north and east of the [HOUSE_CENTRE]
     */
    private fun Player.buildRange(): IntRange {
        val size = 3 + sizeLevels.count { levels.get(Skill.Construction) >= it }
        return HOUSE_CENTRE - (size - 1) / 2..HOUSE_CENTRE + size / 2
    }

    private fun Player.maxRooms(): Int = 20 + roomLevels.count { levels.get(Skill.Construction) >= it }

    private suspend fun Player.preview() {
        val base = houseBase()
        if (base == null || !inOwnHouse()) {
            clearPreview()
            return
        }
        val position: Int = get("house_preview_position") ?: return
        val room: String = get("house_preview_room") ?: return
        dynamicZones.copy(Tables.tile("house_rooms.$room.template").zone, roomZone(base, position), get("house_preview_rotation", 0))
        choice {
            option("Rotate clockwise") {
                rotate(1)
            }
            option("Rotate anticlockwise") {
                rotate(3)
            }
            option("Build") {
                build()
            }
            option("Cancel") {
                clearPreview()
            }
        }
    }

    private suspend fun Player.rotate(turns: Int) {
        set("house_preview_rotation", (get("house_preview_rotation", 0) + turns) % 4)
        preview()
    }

    private fun Player.build() {
        clearWalkTrigger()
        val position: Int = remove("house_preview_position") ?: return
        val room: String = remove("house_preview_room") ?: return
        val rotation: Int = remove("house_preview_rotation") ?: 0
        val base = houseBase()
        if (base == null || !inOwnHouse()) {
            return
        }
        if (inventory.remove("coins", Rows.get("house_rooms.$room").int("cost"))) {
            addHouseRoom(room, position, rotation)
        } else {
            message("You don't have enough coins to build this room.") // TODO proper message
        }
        loadHouse(base, buildMode = true)
    }

    private suspend fun Player.removeRoom(position: Int, room: String) {
        if (exitPortals(position) > 0 && exitPortals() <= exitPortals(position)) {
            message("Your house must have at least one exit portal.") // TODO proper message
            return
        }
        choice("Remove the ${room.replace('_', ' ')}?") {
            option("Yes") {
                val base = houseBase()
                if (base == null || !inOwnHouse() || position !in houseRoomPositions) {
                    return@option
                }
                removeHouseRoom(position)
                loadHouse(base, buildMode = true)
                message("Room deleted!")
            }
            option("No")
        }
    }

    /**
     * Remove the room being previewed
     */
    private fun Player.clearPreview() {
        clearWalkTrigger()
        clear("house_preview_room")
        clear("house_preview_rotation")
        clear("house_preview_position")
        val base = houseBase() ?: return
        loadHouse(base, buildMode = true)
    }

    companion object {
        // Levels at which the buildable area grows from 3x3 up to 7x7 rooms
        private val sizeLevels = intArrayOf(15, 30, 45, 60)

        // Levels at which the maximum number of rooms increases from 21 up to 33
        private val roomLevels = intArrayOf(1, 38, 44, 50, 56, 62, 68, 74, 80, 86, 92, 96, 99)
    }
}
