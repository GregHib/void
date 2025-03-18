package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader

/**
 * @param level required to attempt to mine
 * @param ores List of materials that can be mined
 * @param life duration in ticks that the fire object will last for
 * @param gems if rock has chance of dropping random gems
 */
data class Rock(
    val level: Int = 1,
    val ores: List<String> = emptyList(),
    val life: Int = -1,
    val gems: Boolean = false
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Rock {
            var level = 1
            val ores = ObjectArrayList<String>(1)
            var life = -1
            var gems = false
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "ores" -> while (reader.nextElement()) {
                        ores.add(reader.string())
                    }
                    "life" -> life = reader.int()
                    "gems" -> gems = reader.boolean()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Rock(level = level, ores = ores, life = life, gems = gems)
        }

        val EMPTY = Rock()
    }
}