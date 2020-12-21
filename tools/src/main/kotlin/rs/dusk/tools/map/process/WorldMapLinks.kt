package rs.dusk.tools.map.process

import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.update.task.viewport.Spiral
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.io.File

/**
 * Grabs world map links (areas joined by yellow lines) and converts them to [NavigationGraph] [Link]'s
 */
class WorldMapLinks(
    private val graph: NavigationGraph,
    private val objectDecoder: ObjectDecoder
) : Pipeline.Modifier<Map<Tile, List<GameObject>>> {
    override fun process(content: Map<Tile, List<GameObject>>) {
        val script = File("${System.getProperty("user.home")}\\Documents\\decompiled cs2\\295.cs2")
        // Bi-directional
        var regex = "script_297\\(location\\((.*?),\\s(.*?),\\s(.*?)\\), location\\((.*?),\\s(.*?),\\s(.*?)\\)".toRegex()
        regex.findAll(script.readText()).forEach {
            val vals = it.groupValues
            val x = vals[1].toInt()
            val y = vals[2].toInt()
            val z = vals[3].toInt()
            val x2 = vals[4].toInt()
            val y2 = vals[5].toInt()
            val z2 = vals[6].toInt()

            val ones = content.getObjectsNear(Tile(x, y, z))
            val twos = content.getObjectsNear(Tile(x2, y2, z2))
            if (ones.isNotEmpty() && twos.isNotEmpty()) {
                val start = ones.first()
                var link = graph.addLink(x, y, z, x2, y2, z2)
                link.actions = mutableListOf("object ${start.id} ${getFirstOption(start.id)}")
                val end = twos.first()
                link = graph.addLink(x2, y2, z2, x, y, z)
                link.actions = mutableListOf("object ${end.id} ${getFirstOption(end.id)}")
            } else {
                println("Unknown $x, $y, $z -> $x2, $y2, $z2")
            }
        }
        // One-way
        regex = "script_298\\(location\\((.*?),\\s(.*?),\\s(.*?)\\), location\\((.*?),\\s(.*?),\\s(.*?)\\)".toRegex()
        regex.findAll(script.readText()).forEach {
            val vals = it.groupValues
            val x = vals[1].toInt()
            val y = vals[2].toInt()
            val z = vals[3].toInt()
            val x2 = vals[4].toInt()
            val y2 = vals[5].toInt()
            val z2 = vals[6].toInt()
            val objects = content.getObjectsNear(Tile(x, y, z))
            if (objects.isNotEmpty()) {
                val start = objects.first()
                val link = graph.addLink(x, y, z, x2, y2, z2)
                link.actions = mutableListOf("object ${start.id} ${getFirstOption(start.id)}")
            } else {
                println("Unknown $x, $y, $z -> $x2, $y2, $z2")
            }
        }
        println("${graph.links.size} world map links found.")
    }

    private val interactive: (GameObject) -> Boolean = {
        getFirstOption(it.id) != null
    }
    private fun Map<Tile, List<GameObject>>.getObjectsNear(tile: Tile): List<GameObject> {
        val list = mutableListOf<GameObject>()
        Spiral.spiral(tile, 4) { t ->
            list.addAll(this[t] ?: return@spiral)
        }
        return list.filter(interactive)
    }

    private fun getFirstOption(id: Int): String? {
        val def = objectDecoder.get(id)
        var option = def.options.firstOrNull { it != null && it != "Examine" }
        if (option == null) {
            def.configObjectIds?.forEach { id ->
                option = objectDecoder.get(id).options.firstOrNull { it != null && it != "Examine" }
                if (option != null) {
                    return option
                }
            }
        }
        return option
    }
}