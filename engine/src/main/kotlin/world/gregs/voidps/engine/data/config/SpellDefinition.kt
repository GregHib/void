package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Params

data class SpellDefinition(
    val maxHit: Int = 0,
    val experience: Double = 0.0,
    override var stringId: String = "",
    override var extras: Map<Int, Any>? = null,
) : Extra {
    companion object {
        operator fun invoke(key: String, map: Map<Int, Any>): SpellDefinition {
            val extras = map.toMutableMap()
            val damage = extras.remove(Params.MAX_HIT) as? Int ?: 0
            val experience = extras.remove(Params.EXP) as Double
            return SpellDefinition(damage, experience, key, extras)
        }
    }
}
