package world.gregs.voidps.engine.data.definition.data

/**
 * @param level required to clean
 * @param xp experience from cleaning a grimy herb
 */
data class Cleaning(
        val level: Int = 1,
        val xp: Double = 0.0
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Cleaning(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["clean_xp"] as? Double ?: EMPTY.xp,
        )

        val EMPTY = Cleaning()
    }
}