package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Parameterized

data class PrayerDefinition(
    val index: Int = -1,
    val level: Int = 1,
    val drain: Int = 0,
    val groups: List<Int> = emptyList(),
    val drains: Map<String, Int> = emptyMap(),
    val bonuses: Map<String, Int> = emptyMap(),
    val members: Boolean = false,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Parameterized {
    companion object {
        val EMPTY = PrayerDefinition()
    }
}
