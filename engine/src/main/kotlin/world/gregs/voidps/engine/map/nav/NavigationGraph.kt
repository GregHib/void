package world.gregs.voidps.engine.map.nav

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.koin.dsl.module
import world.gregs.voidps.engine.map.Tile

val navModule = module {
    single(createdAtStart = true) { GraphLoader(get(), get()).run(getProperty("navGraph")) }
}

class NavigationGraph(
    private val adjacencyList: Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>,
) {
    // Temp for demo
    val tiles = adjacencyList.filterKeys { it is Tile }

    val size = adjacencyList.size

    fun contains(node: Any) = adjacencyList.containsKey(node)

    fun getAdjacent(node: Any): Set<Edge> = adjacencyList.getOrDefault(node, empty)

    fun get(node: Any) = adjacencyList.getOrDefault(node, empty)

    fun add(node: Any, set: ObjectOpenHashSet<Edge>) {
        adjacencyList[node] = set
    }

    fun remove(node: Any) {
        adjacencyList.remove(node)
    }

    companion object {
        private val empty = ObjectOpenHashSet<Edge>()
        private val reader = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }
}