package content.entity.obj.ship

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad
import kotlin.collections.set

class CharterShips {
    private lateinit var prices: Map<String, Map<String, Int>>

    fun get(name: String): Map<String, Int> = prices.getOrDefault(name, emptyMap())

    fun get(name: String, target: String): Int? = prices[name]?.get(target)

    fun load(path: String): CharterShips {
        timedLoad("charter ships") {
            val prices = Object2ObjectOpenHashMap<String, Map<String, Int>>(10)
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    val locations = Object2IntOpenHashMap<String>(10)
                    while (nextPair()) {
                        locations[key()] = int()
                    }
                    prices[stringId] = locations
                }
            }
            this.prices = prices
            prices.size
        }
        return this
    }
}
