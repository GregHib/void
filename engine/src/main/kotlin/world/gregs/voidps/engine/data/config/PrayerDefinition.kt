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

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(key: String, map: Map<String, Any>): PrayerDefinition {
            val extras = map.toMutableMap()
            val index = extras.remove("index") as? Int ?: EMPTY.index
            val level = extras.remove("level") as? Int ?: EMPTY.level
            val drain = extras.remove("drain") as? Int ?: EMPTY.drain
            val groups = extras.remove("groups") as? List<Int> ?: EMPTY.groups
            val bonuses = extras.remove("bonuses") as? Map<String, Int> ?: EMPTY.bonuses
            val members = extras.remove("members") as? Boolean ?: EMPTY.members
            return PrayerDefinition(index, level, drain, groups, bonuses, members, key, extras)
        }
    }
}