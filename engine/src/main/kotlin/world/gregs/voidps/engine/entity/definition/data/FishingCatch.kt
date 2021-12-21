package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.utility.toIntRange

data class FishingCatch(
    val level: Int,
    val xp: Double,
    val chance: IntRange
) {
    companion object {
        operator fun invoke(map: Any) = invoke(map as Map<String, Any>)

        operator fun invoke(map: Map<String, Any>) = FishingCatch(
            level = map["level"] as? Int ?: 1,
            xp = map["xp"] as? Double ?: 0.0,
            chance = (map["chance"] as? String)?.toIntRange() ?: 0..0
        )

        val EMPTY = FishingCatch(1, 0.0, 1..1)
    }
}