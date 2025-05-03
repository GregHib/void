package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param level required to make item
 * @param xp experience per item
 * @param animation the animation the player will perform when fletching the item
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

        operator fun invoke(reader: ConfigReader): Fletching {
            var level = 1
            var xp = 0.0
            var animation = ""
            var makeAmount = 1
            var tick = -1
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "animation" -> animation = reader.string()
                    "make_amount" -> makeAmount = reader.int()
                    "tick" -> tick = reader.int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Fletching(level = level, xp = xp, animation = animation, makeAmount = makeAmount, tick = tick)
        }

        val EMPTY = Fletching()
    }
}