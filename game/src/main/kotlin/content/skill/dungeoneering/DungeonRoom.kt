package content.skill.dungeoneering

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

data class DungeonRoom(val tile: Tile, val isCritical: Boolean) {
    var type: DungeonRoomType = DungeonRoomType.Normal
    val keys = mutableListOf<String>()
    val doors = arrayOfNulls<DungeonDoor>(4)
    val adjacentRooms = arrayOfNulls<DungeonRoom>(4)
    var parent: DungeonRoom? = null

    var open: Boolean = false
    var zone: Zone? = null
    var rotation: Int = 0
}
