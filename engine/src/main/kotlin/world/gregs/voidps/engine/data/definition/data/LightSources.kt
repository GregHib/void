package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param onceLit the item that is given once the player lights a light source
 * @param onceExtinguish the item that is given once the player extinguishes the light source
 * @param level the firemaking level required to light the light source
 */
data class LightSources(
    val onceLit: String = "",
    val onceExtinguish: String = "",
    val level: Int = -1
) {
    companion object {

        operator fun invoke(reader: ConfigReader): LightSources {
            var onceLit = ""
            var onceExtinguish = ""
            var level = -1
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "once_lit" -> onceLit = reader.string()
                    "once_extinguish" -> onceExtinguish = reader.string()
                    "level" -> level = reader.int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return LightSources(onceLit = onceLit, onceExtinguish = onceExtinguish, level = level)
        }

        val EMPTY = LightSources()
    }
}