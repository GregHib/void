package content.skill.construction

import content.skill.construction.HouseGrid.DUNGEON_LEVEL
import content.skill.construction.HouseGrid.roomAbove
import content.skill.construction.HouseGrid.roomBelow
import content.skill.construction.HouseGrid.roomLevel
import content.skill.construction.HouseHotspots.addHouseFurniture
import content.skill.construction.HouseHotspots.houseFurniture
import content.skill.construction.HouseHotspots.houseFurnitureHotspots
import content.skill.construction.HouseHotspots.houseFurnitureIds
import content.skill.construction.HouseHotspots.houseFurnitureRooms
import content.skill.construction.HouseHotspots.removeHouseFurniture
import content.skill.construction.HouseRooms.houseRoom
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Furniture which connects floors: staircases, the dungeon entrance and throne room trapdoors with oubliette ladders.
 * Each has a matching piece in the room above or below which is built and removed along with it.
 */
object HouseConnections {
    val trapdoors = listOf("trapdoor", "trapdoor_2", "trapdoor_3")
    val ladders = listOf("oak_ladder", "teak_ladder", "mahogany_ladder")
    private const val TRAPDOOR_SPACE = "throne_room_trapdoor_space"
    private const val LADDER_SPACE = "oubliette_ladder_space"

    fun Player.houseStairs(position: Int): String? = stairsIndex(position)?.let { houseFurnitureIds[it] }

    private fun Player.stairsIndex(position: Int): Int? {
        val staircases = Tables.itemList("house_hotspots.skill_hall_stair_space.furniture")
        val rooms = houseFurnitureRooms
        val ids = houseFurnitureIds
        return rooms.indices.firstOrNull { rooms[it] == position && ids[it] in staircases }
    }

    fun Player.stairsDown(position: Int): Boolean {
        val index = stairsIndex(position) ?: return false
        return houseFurnitureHotspots[index] == Tables.objOrNull("house_rooms.${houseRoom(position)}.stairs_down")
    }

    fun Player.removeHouseStairs(position: Int) {
        val index = stairsIndex(position) ?: return
        removeHouseFurniture(position, houseFurnitureHotspots[index])
    }

    fun Player.hasEntrance(position: Int): Boolean {
        val rooms = houseFurnitureRooms
        val ids = houseFurnitureIds
        return rooms.indices.any { rooms[it] == position && ids[it] == "dungeon_entrance" }
    }

    /**
     * Adds the other end of the stairs leading down from the room above, or up from the room below,
     * to the room at [position] if it has space for them. Dungeon rooms below a dungeon entrance get stairs up to it.
     * Returns whether stairs were added.
     */
    fun Player.connectStairs(position: Int): Boolean {
        val room = houseRoom(position) ?: return false
        if (connectLadder(position)) {
            return true
        }
        if (houseStairs(position) != null) {
            return false
        }
        val above = roomAbove(position)
        val below = roomBelow(position)
        val (furniture, hotspot) = when {
            houseStairs(above) != null && stairsDown(above) -> houseStairs(above) to Tables.objOrNull("house_rooms.$room.stairs")
            houseStairs(below) != null && !stairsDown(below) -> houseStairs(below) to Tables.objOrNull("house_rooms.$room.stairs_down")
            roomLevel(position) == DUNGEON_LEVEL && hasEntrance(above) -> "oak_staircase" to Tables.objOrNull("house_rooms.$room.stairs")
            else -> return false
        }
        if (furniture == null || hotspot == null) {
            return false
        }
        addHouseFurniture(position, hotspot, furniture)
        return true
    }

    fun Player.houseTrapdoor(position: Int): String? = houseFurniture(position, TRAPDOOR_SPACE)

    fun Player.houseLadder(position: Int): String? = houseFurniture(position, LADDER_SPACE)

    /**
     * Adds the ladder matching the trapdoor in the throne room above to the oubliette at [position].
     * Returns whether a ladder was added.
     */
    private fun Player.connectLadder(position: Int): Boolean {
        if (houseRoom(position) != "oubliette" || houseLadder(position) != null) {
            return false
        }
        val trapdoor = houseTrapdoor(roomAbove(position)) ?: return false
        addHouseFurniture(position, LADDER_SPACE, ladders.getOrNull(trapdoors.indexOf(trapdoor)) ?: return false)
        return true
    }

    /**
     * Removes the other end of the trapdoor or ladder [furniture] built in the room at [position]
     */
    fun Player.removeLadder(position: Int, furniture: String) {
        when (furniture) {
            in trapdoors -> removeHouseFurniture(roomBelow(position), LADDER_SPACE)
            in ladders -> removeHouseFurniture(roomAbove(position), TRAPDOOR_SPACE)
        }
    }
}
