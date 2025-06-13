package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param level smithing level required to smith
 * @param xp experience for smithing
 */
data class Smithing(
    val level: Int = 0,
    val xp: Double = 0.0,
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Smithing {
            var level = 0
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Smithing(level = level, xp = xp)
        }

        val EMPTY = Smithing()
    }
}
