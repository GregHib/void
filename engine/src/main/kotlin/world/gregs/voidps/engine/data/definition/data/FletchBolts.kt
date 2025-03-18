package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param level required to make bolt
 * @param xp experience per bolt made
 */
data class FletchBolts(
    val level: Int = 1,
    val xp: Double = 0.0
) {
    companion object {

        operator fun invoke(reader: ConfigReader): FletchBolts {
            var level = 1
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return FletchBolts(level = level, xp = xp)
        }

        val EMPTY = FletchBolts()
    }
}