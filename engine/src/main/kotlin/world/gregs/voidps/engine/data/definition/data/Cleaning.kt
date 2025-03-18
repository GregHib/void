package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param level required to clean
 * @param xp experience from cleaning a grimy herb
 */
data class Cleaning(
    val level: Int = 1,
    val xp: Double = 0.0
) {
    companion object {

        operator fun invoke(reader: ConfigReader): Cleaning {
            var level = 1
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "clean_xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Cleaning(level = level, xp = xp)
        }

        val EMPTY = Cleaning()
    }
}