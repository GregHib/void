package world.gregs.voidps.engine.map.nav

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.instruct.Walk

class GraphLoader(
    private val loader: FileLoader,
    private val definitions: ObjectDefinitions
) : TimedLoader<NavigationGraph>("ai nav graph") {

    override fun load(args: Array<out Any?>): NavigationGraph {
        val path = args[0] as String
        val list: List<Map<String, Any>> = loader.load(path)
        val map = Object2ObjectOpenHashMap<Any, ObjectOpenHashSet<Edge>>()
        for (entry in list) {
            val node = toTile(entry["tile"] as List<Int>)
            val edges = entry["edges"] as List<Map<String, Any>>
            val set = ObjectOpenHashSet<Edge>()
            for (edge in edges) {
                set.add(toEdge(node, edge))
            }
            map[node] = set
        }
        return NavigationGraph(map)
    }

    private fun toTile(list: List<Int>): Tile {
        return Tile(list[0], list[1], list.getOrNull(2) ?: 0)
    }

    private fun toEdge(start: Tile, map: Map<String, Any>): Edge {
        val tile = toTile(map["tile"] as List<Int>)
        val cost = map["cost"] as Int
        val steps = map["steps"] as List<Map<String, Any>>
        return Edge(start, tile, cost, steps.mapNotNull { toInstruction(it) })
    }

    private fun toInstruction(map: Map<String, Any>): Instruction? {
        when (map["type"] as String) {
            "walk" -> {
                val tile = map["tile"] as List<Int>
                val x = tile[0]
                val y = tile[1]
                return Walk(x, y)
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
}