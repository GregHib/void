package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange

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
            var chance = 0..0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "xp" -> xp = reader.double()
                    "chance" -> chance = reader.string().toIntRange()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Ore(xp = xp, chance = chance)
        }

        val EMPTY = Ore()
    }
}