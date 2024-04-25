package world.gregs.voidps.engine.data.definition.data

/**
 * @param level required to make dart
 * @param xp experience per dart made
 */
data class FletchDarts(
    val level: Int = 1,
    val xp: Double = 0.0
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = FletchDarts(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        val EMPTY = FletchDarts()
    }
}