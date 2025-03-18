package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange

/**
 * @param level required to attempt to light
 * @param xp experience from successfully lighting a fire
 * @param chance Chance of creating a fire at level 1 and 99
 * @param life duration in ticks that the fire object will last for
 */
data class Fire(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 65..513,
    val life: Int = 0,
    val colour: String = "orange"
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Fire {
            var level = 1
            var xp = 0.0
            var chance = 65..513
            var life = 0
            var colour = "orange"
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "chance" -> chance = reader.string().toIntRange()
                    "life" -> life = reader.int()
                    "colour" -> colour = reader.string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Fire(level = level, xp = xp, chance = chance, life = life, colour = colour)
        }

        val EMPTY = Fire()
    }
}