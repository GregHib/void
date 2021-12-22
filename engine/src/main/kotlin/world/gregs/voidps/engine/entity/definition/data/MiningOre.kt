package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.utility.toIntRange

/**
 * @param xp experience for successful mining
 * @param chance of mining per cycle
 */
data class MiningOre(
    val xp: Double = 0.0,
    val chance: IntRange = 0..0
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = MiningOre(
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = (map["chance"] as? String)?.toIntRange() ?: EMPTY.chance
        )

        val EMPTY = MiningOre()
    }
}