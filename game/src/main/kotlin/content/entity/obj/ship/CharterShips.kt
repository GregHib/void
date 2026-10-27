package content.entity.obj.ship

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import kotlin.collections.set

class CharterShips {
    private lateinit var prices: Map<String, Map<String, Int>>

    fun get(name: String): Map<String, Int> = prices.getOrDefault(name, emptyMap())

    fun get(name: String, target: String): Int? = prices[name]?.get(target)

    fun load(path: String): CharterShips {
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
        return this
    }

    fun copyFrom(other: CharterShips) {
        prices = other.prices
    }
}

object CharterShipsLoader : Loader<CharterShips> {
    val instance = CharterShips()
    override val name = "charter_ships"
    override val file = listOf("map.ships.prices")

    override fun load(context: LoadContext): CharterShips = CharterShips().load(context.paths(this).single())

    override fun install(state: CharterShips) = instance.copyFrom(state)
}
