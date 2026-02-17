package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

data class Tiara(
    val xp: Double = 0.0,
    val level: Int = 1,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tiara

        if (xp != other.xp) return false
        return level == other.level
    }

    override fun hashCode(): Int {
        var result = xp.hashCode()
        result = 31 * result + level

        return result
    }

    companion object {
        operator fun invoke(reader: ConfigReader): Tiara {
            var xp = 0.0
            var level = 1

            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "xp" -> xp = reader.double()
                    "level" -> level = reader.int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }

            return Tiara(xp = xp, level = level)
        }
    }
}