package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param level required to make dart
 * @param xp experience per dart made
 */
data class FletchDarts(
    val level: Int = 1,
    val xp: Double = 0.0,
) {
    companion object {

        operator fun invoke(reader: ConfigReader): FletchDarts {
            var level = 1
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return FletchDarts(level = level, xp = xp)
        }

        val EMPTY = FletchDarts()
    }
}
