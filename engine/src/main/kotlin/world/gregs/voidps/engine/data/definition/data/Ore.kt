package world.gregs.voidps.engine.data.definition.data

/**
 * @param xp experience for successful mining
 * @param chance of mining per cycle
 */
data class Ore(
    val xp: Double = 0.0,
    val chance: IntRange = 0..0
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Ore(
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = map["chance"] as? IntRange ?: EMPTY.chance
        )

        val EMPTY = Ore()
    }
}