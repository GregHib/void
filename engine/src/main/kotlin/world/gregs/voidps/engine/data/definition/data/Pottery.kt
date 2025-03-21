package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader

data class Pottery(
    val map: Map<String, Ceramic> = emptyMap()
) {

    data class Ceramic(
        val level: Int = 1,
        val xp: Double = 0.0
    ) {
        companion object {
            val EMPTY = Ceramic()
        }
    }

    companion object {
        operator fun invoke(reader: ConfigReader): Pottery {
            val map = Object2ObjectOpenHashMap<String, Ceramic>(1, Hash.VERY_FAST_LOAD_FACTOR)
            while (reader.nextEntry()) {
                val item = reader.key()
                var level = 1
                var xp = 0.0
                while (reader.nextEntry()) {
                    when (val key = reader.key()) {
                        "level" -> level = reader.int()
                        "xp" -> xp = reader.double()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                    }
                }
                map[item] = Ceramic(level, xp)
            }
            return Pottery(map)
        }
        val EMPTY = Pottery()
    }
}