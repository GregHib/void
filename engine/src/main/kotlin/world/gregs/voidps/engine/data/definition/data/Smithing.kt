package world.gregs.voidps.engine.data.definition.data

/**
 * @param level smithing level required to smith
 * @param xp experience for smithing
 */
data class Smithing(
    val level: Int = 0,
    val xp: Double = 0.0
) {
    companion object {
        operator fun invoke(map: Map<String, Any>) = Smithing(
            level = map["level"] as Int,
            xp = map["xp"] as Double
        )
        val EMPTY = Smithing()
    }
}