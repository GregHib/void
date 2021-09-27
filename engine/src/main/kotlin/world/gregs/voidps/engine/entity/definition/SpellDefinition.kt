package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.Extra

data class SpellDefinition(
    val maxHit: Int = 0,
    val experience: Double = 0.0,
    override var extras: Map<String, Any> = emptyMap()
) : Extra {
    companion object {
        operator fun invoke(map: Map<String, Any>): SpellDefinition {
            val extras = map.toMutableMap()
            val damage = extras.remove("max_hit") as Int
            val experience = extras.remove("exp") as Double
            return SpellDefinition(damage, experience, extras)
        }
    }
}