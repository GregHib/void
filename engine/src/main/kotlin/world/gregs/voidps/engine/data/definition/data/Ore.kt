package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param xp experience for successful mining
 * @param chance of mining per cycle
 */
data class Ore(
    val xp: Double = 0.0,
    val chance: IntRange = 0..0
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Ore {
            var xp = 0.0
            var chanceMin = 0
            var chanceMax = 0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "xp" -> xp = reader.double()
                    "chance_min" -> chanceMin = reader.int()
                    "chance_max" -> chanceMax = reader.int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Ore(xp = xp, chance = chanceMin until chanceMax)
        }

        val EMPTY = Ore()
    }
}