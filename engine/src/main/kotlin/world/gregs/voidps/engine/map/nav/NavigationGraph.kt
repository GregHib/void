package world.gregs.voidps.engine.map.nav

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.koin.dsl.module
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.getProperty
import java.io.File

val navModule = module {
    single(createdAtStart = true) { NavigationGraph(get()) }
}

class NavigationGraph(
    private val definitions: ObjectDefinitions
) {

    private lateinit var adjacencyList: Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>
    private lateinit var tags: Map<Any, Set<String>>

    val nodes: Set<Any>
        get() = adjacencyList.keys
    val size: Int
        get() = adjacencyList.size

    fun contains(node: Any) = adjacencyList.containsKey(node)

    fun getAdjacent(node: Any): Set<Edge> = adjacencyList.getOrDefault(node, empty)

    fun get(node: Any): ObjectOpenHashSet<Edge> = adjacencyList.getOrPut(node) { ObjectOpenHashSet() }

    fun tags(node: Any): Set<String> = tags[node] ?: emptyTags

    fun add(node: Any, set: ObjectOpenHashSet<Edge>) {
        adjacencyList[node] = set
    }

    fun remove(node: Any) {
        adjacencyList.remove(node)
    }

    init {
        load()
    }

    fun load() = timedLoad("ai nav graph") {
        val path = getProperty("navGraphPath")
        val options = LoaderOptions()
        options.maxAliasesForCollections = Int.MAX_VALUE
        val yaml = Yaml(options)
        // Jackson yaml doesn't support anchors - https://github.com/FasterXML/jackson-dataformats-text/issues/98
        val data: Map<String, Any> = yaml.load(File(path).readText(Charsets.UTF_8))
        val edges = data["edges"] as Map<String, Any>
        val map = Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>()
        flatten("", edges) { path, edges ->
            for (edge in edges) {
                val start = toTile(edge["from"] as List<Int>)
                val end = toTile(edge["to"] as List<Int>)
                var cost = edge["cost"] as? Int ?: 0
                val steps = edge["steps"] as? List<Map<String, Any>>
                val walk = steps == null
                val instructions = if (steps == null) {
                    cost = Distance.manhattan(start.x, start.y, end.x, end.y)
                    listOf(Walk(end.x, end.y))
                } else {
                    steps.mapNotNull { toInstruction(it) }
                }
                map.getOrPut(start) { ObjectOpenHashSet() }.add(Edge(path, start, end, cost, instructions))
                if (walk) {
                    map.getOrPut(end) { ObjectOpenHashSet() }.add(Edge(path, end, start, cost, listOf(Walk(start.x, start.y))))
                }
            }
        }
        val tags: Map<Any, Set<String>> = (data["tags"] as List<Map<String, Any>>).map { toTile(it["tile"] as List<Int>) to (it["tags"] as List<String>).toSet() }.toMap()
        this.adjacencyList = map
        this.tags = tags
        edges.size
    }

    private fun flatten(path: String, map: Map<String, Any>, process: (String, List<Map<String, Any>>) -> Unit): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                list.addAll(flatten("$path $key", value as Map<String, Any>, process))
            } else if (value is List<*>) {
                process.invoke(path, value as List<Map<String, Any>>)
            }
        }
        return list
    }

    private fun toTile(list: List<Int>): Tile {
        return Tile(list[0], list[1], list.getOrNull(2) ?: 0)
    }

    private fun toInstruction(map: Map<String, Any>): Instruction? {
        when (map["type"] as String) {
            "walk" -> {
                val tile = map["tile"] as List<Int>
                return Walk(tile[0], tile[1])
            }
            "object" -> {
                val objectId = map["object"] as Int
                val tile = map["tile"] as List<Int>
                val x = tile[0]
                val y = tile[1]
                val option = map["option"] as String
                val def = definitions.getOrNull(objectId) ?: return null
                val optionIndex = def.options.indexOf(option) + 1
                return InteractObject(objectId, x, y, optionIndex)
            }
        }
        return null
    }

    companion object {
        private val empty = emptySet<Edge>()
        private val emptyTags = emptySet<String>()
    }

}