package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Extra

data class PrayerDefinition(
    val index: Int = -1,
    val level: Int = 1,
    val drain: Int = 0,
    val groups: List<Int> = emptyList(),
    val bonuses: Map<String, Int> = emptyMap(),
    val members: Boolean = false,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Extra {
    companion object {
        val EMPTY = PrayerDefinition()
    }
}