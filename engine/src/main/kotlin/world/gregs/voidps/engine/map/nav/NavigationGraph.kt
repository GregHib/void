package world.gregs.voidps.engine.map.nav

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.koin.dsl.module
import world.gregs.voidps.engine.map.Tile

val navModule = module {
    single(createdAtStart = true) { GraphLoader(get()).run(getProperty("navGraph")) }
}

class NavigationGraph(
    private val adjacencyList: Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>,
) {
    // Temp for demo
    val tiles = adjacencyList.filterKeys { it is Tile }
    val nodes: Set<Any>
        get() = adjacencyList.keys

    val size = adjacencyList.size

    fun contains(node: Any) = adjacencyList.containsKey(node)

    fun getAdjacent(node: Any): Set<Edge> = adjacencyList.getOrDefault(node, empty)

    fun get(node: Any): ObjectOpenHashSet<Edge> = adjacencyList.getOrPut(node) { ObjectOpenHashSet() }

    fun add(node: Any, set: ObjectOpenHashSet<Edge>) {
        adjacencyList[node] = set
    }

    fun remove(node: Any) {
        adjacencyList.remove(node)
    }

    companion object {
        private val empty = emptySet<Edge>()
    }
}