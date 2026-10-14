package content.skill.construction

import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

/**
 * The furniture saved for a players house, stored as parallel lists of the room, hotspot and furniture built on it
 */
object HouseHotspots {
    val Player.houseFurnitureRooms: List<Int>
        get() = get("house_furniture_rooms") ?: emptyList()

    val Player.houseFurnitureHotspots: List<String>
        get() = get("house_furniture_hotspots") ?: emptyList()

    val Player.houseFurnitureIds: List<String>
        get() = get("house_furniture_ids") ?: emptyList()

    /**
     * The hotspot [obj] belongs to, hotspots in a group share the group name
     */
    fun hotspot(obj: GameObject): String? {
        val row = Rows.getOrNull("house_hotspots.${obj.id}") ?: return null
        return row.stringOrNull("group") ?: obj.id
    }

    fun Player.houseFurniture(position: Int, hotspot: String): String? {
        val rooms = houseFurnitureRooms
        val hotspots = houseFurnitureHotspots
        val index = rooms.indices.firstOrNull { rooms[it] == position && hotspots[it] == hotspot } ?: return null
        return houseFurnitureIds[index]
    }

    /**
     * Number of exit portals in the house, or only those in the room at [position]
     */
    fun Player.exitPortals(position: Int? = null): Int {
        val rooms = houseFurnitureRooms
        val ids = houseFurnitureIds
        return ids.indices.count { ids[it] == "exit_portal" && (position == null || rooms[it] == position) }
    }

    fun Player.addHouseFurniture(position: Int, hotspot: String, id: String) {
        set("house_furniture_rooms", houseFurnitureRooms + position)
        set("house_furniture_hotspots", houseFurnitureHotspots + hotspot)
        set("house_furniture_ids", houseFurnitureIds + id)
    }

    /**
     * Removes the furniture built in the room at [position], or only that built on [hotspot], resetting their hotspots variants
     */
    fun Player.removeHouseFurniture(position: Int, hotspot: String? = null) {
        val rooms = houseFurnitureRooms
        val hotspots = houseFurnitureHotspots
        val keep = rooms.indices.filter { rooms[it] != position || (hotspot != null && hotspots[it] != hotspot) }
        for (index in rooms.indices - keep.toSet()) {
            val variant = Tables.stringOrNull("house_hotspots.${hotspots[index]}.variant") ?: continue
            clear(variant)
        }
        val ids = houseFurnitureIds
        set("house_furniture_rooms", keep.map { rooms[it] })
        set("house_furniture_hotspots", keep.map { hotspots[it] })
        set("house_furniture_ids", keep.map { ids[it] })
    }
}
