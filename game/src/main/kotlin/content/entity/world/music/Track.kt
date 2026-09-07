package content.entity.world.music

import world.gregs.voidps.type.Area

data class Track(val id: Int, val name: String, val indexes: List<Int>, val area: List<Area> = emptyList()) {
    val index: Int?
        get() = indexes.firstOrNull()
}
