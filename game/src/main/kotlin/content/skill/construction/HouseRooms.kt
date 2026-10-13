package content.skill.construction

import content.skill.construction.HouseGrid.ROOM_GRID
import content.skill.construction.HouseGrid.START_ROOM
import content.skill.construction.HouseHotspots.removeHouseFurniture
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * The rooms saved for a players house, stored as parallel lists of ids, positions and rotations
 */
object HouseRooms {
    val Player.houseRoomIds: List<String>
        get() = get("house_room_ids") ?: emptyList()

    val Player.houseRoomPositions: List<Int>
        get() = get("house_room_positions") ?: emptyList()

    val Player.houseRoomRotations: List<Int>
        get() = get("house_room_rotations") ?: emptyList()

    fun Player.houseRoom(position: Int): String? {
        val index = houseRoomPositions.indexOf(position)
        return if (index == -1) null else houseRoomIds.getOrNull(index)
    }

    fun Player.addHouseRoom(id: String, position: Int, rotation: Int = 0) {
        set("house_room_ids", houseRoomIds + id)
        set("house_room_positions", houseRoomPositions + position)
        set("house_room_rotations", houseRoomRotations + rotation)
    }

    fun Player.removeHouseRoom(position: Int) {
        val index = houseRoomPositions.indexOf(position)
        if (index == -1) {
            return
        }
        set("house_room_ids", houseRoomIds.filterIndexed { i, _ -> i != index })
        set("house_room_positions", houseRoomPositions.filterIndexed { i, _ -> i != index })
        set("house_room_rotations", houseRoomRotations.filterIndexed { i, _ -> i != index })
        removeHouseFurniture(position)
    }

    /**
     * Replaces any existing rooms with a new house containing the starting garden with its exit portal and a parlour to the north
     */
    fun Player.newHouse() {
        set("house_room_ids", listOf("garden", "parlour"))
        set("house_room_positions", listOf(START_ROOM, START_ROOM + ROOM_GRID))
        set("house_room_rotations", listOf(0, 0))
        set("house_furniture_rooms", listOf(START_ROOM))
        set("house_furniture_hotspots", listOf("garden_centrepiece_space"))
        set("house_furniture_ids", listOf("exit_portal"))
    }

    /**
     * Whether the players saved rooms are present and consistent
     */
    fun Player.hasHouse(): Boolean {
        val size = houseRoomIds.size
        return size > 0 && houseRoomPositions.size == size && houseRoomRotations.size == size
    }
}
