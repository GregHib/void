package world.gregs.voidps.engine.data.definition.data

/**
 * @param level required to make item
 * @param xp experience per item
 * @param animation the animation the player will perform will fletching item
 * @param makeAmount the amount of items fletched from a log
 * @param tick the amount of ticks for fletching an item
 */
data class Fletching(
    val level: Int = 1,
    val xp: Double = 0.0,
    val animation: String = "",
    val makeAmount: Int = 1,
    val tick: Int = -1
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Fletching(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            animation = map["animation"] as? String ?: EMPTY.animation,
            makeAmount = map["make_amount"] as? Int ?: EMPTY.makeAmount,
            tick = map["tick"] as? Int ?: EMPTY.tick,
        )

        val EMPTY = Fletching()
    }
}