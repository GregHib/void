package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader

data class Spot(
    val tackle: List<String> = emptyList(),
    val bait: Map<String, List<String>> = emptyMap()
) {

    companion object {
        operator fun invoke(reader: ConfigReader): Spot {
            val tackle = ObjectArrayList<String>(1)
            val bait = Object2ObjectOpenHashMap<String, List<String>>(1, Hash.VERY_FAST_LOAD_FACTOR)
            while (reader.nextEntry()) {
                val key = reader.key()
                when (key) {
                    "items" -> while (reader.nextElement()) {
                        tackle.add(reader.string())
                    }
                    "bait" -> while (reader.nextEntry()) {
                        val name = reader.key()
                        val items = ObjectArrayList<String>(1)
                        while (reader.nextElement()) {
                            items.add(reader.string())
                        }
                        bait[name] = items
                    }
                }
            }
            return Spot(tackle = tackle, bait = bait)
        }

        val EMPTY = Spot()
    }
}