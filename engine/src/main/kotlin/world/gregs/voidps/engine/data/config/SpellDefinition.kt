package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Params

data class SpellDefinition(
    val maxHit: Int = 0,
    val experience: Double = 0.0,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Parameterized {
    companion object {
        operator fun invoke(key: String, map: Map<Int, Any>): SpellDefinition {
            val params = map.toMutableMap()
            val damage = params.remove(Params.MAX_HIT) as? Int ?: 0
            val experience = params.remove(Params.EXP) as Double
            return SpellDefinition(damage, experience, key, params)
        }
    }
}
