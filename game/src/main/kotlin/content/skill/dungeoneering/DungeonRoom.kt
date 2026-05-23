package content.skill.dungeoneering

import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Zone

data class DungeonRoom(
    val x: Int,
    val y: Int,
    val zone: Zone,
    val rotation: Int = 0,
    val type: DungeonRoomType = DungeonRoomType.Normal,
    val keys: MutableSet<String> = mutableSetOf(),
    val doors: Array<DungeonDoor?> = arrayOfNulls(4),
    val adjacent: Array<DungeonRoom?> = arrayOfNulls(4),
) {

    override fun toString(): String {
        return "DungeonRoom(x=$x, y=$y, zone=$zone, rotation=$rotation, type=$type, keys=$keys, doors=${doors.withIndex().filter { it.value != null }.joinToString(prefix = "[", postfix = "]") { "${Direction.cardinal[it.index]} - ${it.value}" }}, adjacent=${adjacent.withIndex().filter { it.value != null }.joinToString(prefix = "[", postfix = "]") { "${Direction.cardinal[it.index]} - ${it.value?.let { "(${it.x}, ${it.y})" }}"}})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DungeonRoom

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}