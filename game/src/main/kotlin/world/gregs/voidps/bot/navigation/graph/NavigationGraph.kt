package world.gregs.voidps.bot.navigation.graph

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class NavigationGraph(
    private val definitions: ObjectDefinitions,
    private val areas: AreaDefinitions
) {

    private var adjacencyList: Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>> = Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>()
    private val tags = Object2ObjectOpenHashMap<Any, Set<AreaDefinition>>()

    val nodes: Set<Any>
        get() = adjacencyList.keys
    val size: Int
        get() = adjacencyList.size

    fun contains(node: Any) = adjacencyList.containsKey(node)

    fun getAdjacent(node: Any): Set<Edge> = adjacencyList.getOrDefault(node, empty)

    fun get(node: Any): ObjectOpenHashSet<Edge> = adjacencyList.getOrPut(node) { ObjectOpenHashSet() }

    fun areas(node: Any): Set<AreaDefinition> = tags[node] ?: emptyTags

    fun add(node: Any, set: ObjectOpenHashSet<Edge>) {
        adjacencyList[node] = set
    }

    fun remove(node: Any) {
        adjacencyList.remove(node)
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["map.navGraph"]): NavigationGraph {
        timedLoad("ai nav graph edge") {
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "steps") {
                        super.add(list, toInstruction(value as Map<String, Any>) ?: return, parentMap)
                    } else {
                        super.add(list, value, parentMap)
                    }
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                    } else {
                        super.set(map, key, when (key) {
                            "tile", "from", "to" -> {
                                val list = value as List<Int>
                                Tile(list[0], list[1], list.getOrNull(2) ?: 0)
                            }
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            val data: Map<String, Any> = yaml.load(path, config)
            val edgeMap = data["edges"] as Map<String, Any>
            val map = Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>()
            var count = 0
            flatten("", edgeMap) { path, edges ->
                for (edge in edges) {
                    count++
                    val start = edge["from"] as Tile
                    val end = edge["to"] as Tile
                    var cost = edge["cost"] as? Int ?: 0
                    val steps = edge["steps"] as? List<Instruction>
                    val conditions = edge["conditions"] as? List<Map<String, Any>>
                    val walk = steps == null
                    val instructions = if (steps == null) {
                        cost = Distance.manhattan(start.x, start.y, end.x, end.y)
                        listOf(Walk(end.x, end.y))
                    } else {
                        steps
                    }
                    map.getOrPut(start) { ObjectOpenHashSet(1) }.add(Edge(path, start, end, cost, instructions, conditions?.mapNotNull { toCondition(it) } ?: emptyList()))
                    if (walk) {
                        map.getOrPut(end) { ObjectOpenHashSet(1) }.add(Edge(path, end, start, cost, listOf(Walk(start.x, start.y))))
                    }
                }
            }
            this.adjacencyList = map
            tagAreas()
            count
        }
        return this
    }

    private fun tagAreas() {
        adjacencyList.forEach { (node, _) ->
            val tile = when (node) {
                is Tile -> node
                is GameObject -> node.tile
                else -> return@forEach
            }
            tags[node] = areas.getAll().filter { it.area.contains(tile) }.toSet()
        }
    }

    @Suppress("UNCHECKED_CAST")
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

    private fun toInstruction(map: Map<String, Any>): Instruction? {
        when (map["type"] as? String) {
            "walk" -> {
                val tile = map["tile"] as Tile
                return Walk(tile.x, tile.y)
            }
            "object" -> {
                val objectId = map["object"] as Int
                val tile = map["tile"] as Tile
                val option = map["option"] as String
                var def = definitions.getOrNull(objectId) ?: return null
                val transform = map["transform"] as? Int
                if (transform != null) {
                    def = definitions.getOrNull(def.transforms!![transform]) ?: return null
                }
                val optionIndex = def.optionsIndex(option) + 1
                return InteractObject(objectId, tile.x, tile.y, optionIndex)
            }
        }
        return null
    }

    private fun toCondition(map: Map<String, Any>): Condition? {
        return when (map["type"] as String) {
            "inventory_item" -> HasInventoryItem(map["item"] as String, map["amount"] as Int)
            else -> null
        }
    }

    companion object {
        private val empty = emptySet<Edge>()
        private val emptyTags = emptySet<AreaDefinition>()
    }

}