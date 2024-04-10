package world.gregs.voidps.engine.data.definition.data

/**
 * @param level required to make bolt
 * @param xp experience per bolt made
 */
data class FletchBolts(
        val level: Int = 1,
        val xp: Double = 0.0
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = FletchBolts(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        val EMPTY = FletchBolts()
    }
}