package world.gregs.voidps.engine.map.nav

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.dsl.module
import world.gregs.voidps.engine.map.Tile
import java.io.File

val navModule = module {
    single(createdAtStart = true) { NavigationGraph.load("./navgraph.json") }
}

class NavigationGraph(
    private val adjacencyList: Map<Tile, IntArray>,
    private val edges: Array<Edge>,
) {

    val size = edges.size

    operator fun get(tile: Tile): IntArray {
        return adjacencyList[tile] ?: emptyArray
    }

    operator fun get(index: Int): Edge? {
        return edges.getOrNull(index)
    }

    companion object {
        private val emptyArray = intArrayOf()
        private val reader = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        fun load(path: String = "./navgraph.json"): NavigationGraph {
            val file = File(path)
            val map: Map<String, ArrayList<Map<String, Any>>> = reader.readValue(file)
            val edges = mutableListOf<Edge>()
            val adjacencyList = mutableMapOf<Tile, IntArray>()
            map.forEach { (key, list) ->
                adjacencyList[Tile(key.toInt())] = list.map {
                    edges.add(Edge(Tile(it["start"] as Int),
                        Tile(it["end"] as Int),
                        it["actions"] as? List<String> ?: emptyList(),
                        it["requirements"] as? List<String> ?: emptyList()
                    ))
                    edges.size - 1
                }.toIntArray()
            }
            return NavigationGraph(adjacencyList, edges.toTypedArray())
        }
    }
}