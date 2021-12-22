package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.utility.toIntRange

data class FishingCatch(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 1..1
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = FishingCatch(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = (map["chance"] as? String)?.toIntRange() ?: EMPTY.chance
        )

        val EMPTY = FishingCatch()
    }
}