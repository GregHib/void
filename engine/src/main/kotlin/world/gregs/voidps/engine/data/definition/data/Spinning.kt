package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

data class Spinning(
    val to: String = "",
    val level: Int = 1,
    val xp: Double = 0.0
) {

    companion object {
        operator fun invoke(reader: ConfigReader): Spinning {
            var to = ""
            var level = 1
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "to" -> to = reader.string()
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Spinning(to = to, level = level, xp = xp)
        }

        val EMPTY = Spinning()
    }
}