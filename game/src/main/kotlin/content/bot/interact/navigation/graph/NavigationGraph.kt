package content.bot.interact.navigation.graph

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile

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

    fun load(path: String): NavigationGraph {
        timedLoad("ai nav graph edge") {
            val map = Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>()
            var count = 0
            Config.fileReader(path) {
                while (nextSection()) {
                    val name = section()
                    var from = Tile.EMPTY
                    var to = Tile.EMPTY
                    var cost = 0
                    val steps = ObjectArrayList<Instruction>()
                    val conditions = ObjectArrayList<Condition>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "from" -> from = readTile()
                            "to" -> to = readTile()
                            "cost" -> cost = int()
                            "steps" -> {
                                while (nextElement()) {
                                    var option = ""
                                    var objectId = ""
                                    var transform: Int? = null
                                    var tile = Tile.EMPTY
                                    while (nextEntry()) {
                                        when (val stepKey = key()) {
                                            "option" -> option = string()
                                            "object" -> objectId = string()
                                            "transform" -> transform = int()
                                            "tile" -> tile = readTile()
                                            else -> throw IllegalArgumentException("Unexpected key: '$stepKey' ${exception()}")
                                        }
                                    }
                                    val instruction = when {
                                        objectId != "" -> {
                                            var def = definitions.getOrNull(objectId) ?: continue
                                            if (transform != null) {
                                                val id = def.transforms?.get(transform) ?: continue
                                                def = definitions.getOrNull(id) ?: continue
                                            }
                                            val optionIndex = def.optionsIndex(option) + 1
                                            InteractObject(def.id, tile.x, tile.y, optionIndex)
                                        }
                                        else -> Walk(tile.x, tile.y)
                                    }
                                    steps.add(instruction)
                                }
                            }
                            "conditions" -> {
                                while (nextElement()) {
                                    require(nextEntry()) { "Expected condition type. ${exception()}" }
                                    require(key() == "type") { "Expected condition type. ${exception()}" }
                                    when (val type = string()) {
                                        "inventory_item" -> {
                                            var item = ""
                                            var amount = 0
                                            while (nextEntry()) {
                                                when (val k = key()) {
                                                    "item" -> item = string()
                                                    "amount" -> amount = int()
                                                    else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                                                }
                                            }
                                            conditions.add(HasInventoryItem(item, amount))
                                        }
                                        else -> throw IllegalArgumentException("Unexpected type: '$type' ${exception()}")
                                    }
                                }
                            }
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    val walk = steps.isEmpty
                    if (steps.isEmpty) {
                        cost = Distance.manhattan(from.x, from.y, to.x, to.y)
                        steps.add(Walk(to.x, to.y))
                    }
                    count++
                    map.getOrPut(from) { ObjectOpenHashSet(1) }.add(Edge(name, from, to, cost, steps, conditions))
                    if (walk) { // Bidirectional
                        map.getOrPut(to) { ObjectOpenHashSet(1) }.add(Edge(name, to, from, cost, listOf(Walk(from.x, from.y)), conditions))
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

    companion object {
        private val empty = emptySet<Edge>()
        private val emptyTags = emptySet<AreaDefinition>()
    }
}

fun ConfigReader.readTile(): Tile {
    var x = 0
    var y = 0
    var level = 0
    while (nextEntry()) {
        when (val key = key()) {
            "x" -> x = int()
            "y" -> y = int()
            "level" -> level = int()
            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
        }
    }
    return Tile(x, y, level)
}