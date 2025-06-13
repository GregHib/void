package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Extra

data class SpellDefinition(
    val maxHit: Int = 0,
    val experience: Double = 0.0,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Extra {
    companion object {
        operator fun invoke(key: String, map: Map<String, Any>): SpellDefinition {
            val extras = map.toMutableMap()
            val damage = extras.remove("max_hit") as? Int ?: 0
            val experience = extras.remove("exp") as Double
            return SpellDefinition(damage, experience, key, extras)
        }
    }
}
