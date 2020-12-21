package rs.dusk.tools.map.process

import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.update.task.viewport.Spiral
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.*
import rs.dusk.engine.map.region.tile.*
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.view.graph.NavigationGraph
import kotlin.math.max

class LadderProcessor(
    val graph: NavigationGraph,
    private val objectDecoder: ObjectDecoder,
    private val mapDefinitions: WorldMapInfoDecoder,
    collisions: Collisions
) : Pipeline.Modifier<Map<Tile, List<GameObject>>> {

    private lateinit var map: Map<Tile, List<GameObject>>
    private val linking = LinkingObjects(collisions)
    var count = 0

    var uncertainty = 0

    private val upPredicate: (GameObject) -> Boolean = {
        val def = objectDecoder.get(it.id)
        (def.climb(false) || def.isTrapdoor()) && linking.isReachable(it)
    }

    private val downPredicate: (GameObject) -> Boolean = {
        objectDecoder.get(it.id).climb(true) && linking.isReachable(it)
    }

    private val predicate: (GameObject) -> Boolean = {
        val def = objectDecoder.get(it.id)
        def.climb() && linking.isReachable(it)
    }

    override fun process(content: Map<Tile, List<GameObject>>) {
        map = content
        val unknowns = mutableSetOf<GameObject>()
        content.forEach { (tile, objs) ->
            objs@ for (obj in objs) {
                val def = obj.def
                if (def.mapDefinitionId != -1) {
                    val mapDef = mapDefinitions.getOrNull(def.mapDefinitionId)
                    if (mapDef != null) {
                        if (mapDef.spriteId == 1772 || mapDef.spriteId == 1773) {
                            println("Found $obj ${mapDef.clientScript}")
                        }
                    }
                }

                val width = obj.size.width
                val height = obj.size.height

                if (def.climb(true)) {
                    val loc = checkForLadder(tile, width, height, true)
                    link(unknowns, graph, obj, loc, 1)
                }

                if (def.climb(false)) {
                    val loc = checkForLadder(tile, width, height, false)
                    link(unknowns, graph, obj, loc, 0)
                }

                if (!def.climb(true) && !def.climb(false) && def.climb()) {
                    val loc = checkForClimb(obj, tile, width, height)
                    link(unknowns, graph, obj, loc, 2)
                    if (loc != null) {
                        var link = graph.addLink(tile, loc.tile)
                        link.actions = mutableListOf("object ${obj.id} ${objectDecoder.get(obj.id).getOption()}")
                        link = graph.addLink(loc.tile, tile)
                        link.actions = mutableListOf("object ${loc.id} ${objectDecoder.get(loc.id).getOption()}")
                    } else {
                        unknowns.add(obj)
                    }
                }
            }
        }
        unknowns.forEach {
            if (!graph.contains(it.tile)) {
                println("Unknown ${it.id} ${objectDecoder.get(it.id).name} ${it.tile}")
                count++
            }
        }
        println("Found ${graph.links.size} unknown $count")
    }

    private fun link(unknowns: MutableSet<GameObject>, graph: NavigationGraph, obj: GameObject, loc: GameObject?, type: Int) {
        if (loc == null) {
            unknowns.add(obj)
            return
        }
        val output = linking.linkedPoints(obj, loc)
        if (output == null) {
            unknowns.add(obj)
        } else {
            val start = output.first
            val end = output.second
            var link = graph.addLink(start, end)
            link.actions = mutableListOf("object ${obj.id} ${
                when (type) {
                    0 -> objectDecoder.get(obj.id).getOption(false)
                    1 -> objectDecoder.get(obj.id).getOption(true)
                    else -> objectDecoder.get(obj.id).getOption()
                }
            }")
            link = graph.addLink(end, start)
            link.actions = mutableListOf("object ${loc.id} ${
                when (type) {
                    0 -> objectDecoder.get(loc.id).getOption(true)
                    1 -> objectDecoder.get(loc.id).getOption(false)
                    else -> objectDecoder.get(loc.id).getOption()
                }
            }")
        }
    }

    private fun checkForLadder(tile: Tile, width: Int, height: Int, up: Boolean): GameObject? {
        var tile = tile
        uncertainty = 0
        var radius = max(width, height)
        if (radius > 1) {
            tile = tile.add(width / 2, height / 2)
            radius *= 2
        }
        val predicate = if (up) upPredicate else downPredicate
        var loc = checkArea(predicate, if (up) tile.plane + 1..3 else tile.plane - 1 downTo 0, tile, radius)
        if (loc == null) {
            loc = checkArea(predicate, tile.plane..tile.plane, tile.add(y = if (up) -dungeonOffset else dungeonOffset), radius)
        }
        return loc
    }

    private fun checkForClimb(obj: GameObject, tile: Tile, width: Int, height: Int): GameObject? {
        var tile = tile
        uncertainty = 0
        val radius = 4
        if (radius > 1) {
            tile = tile.add(width / 2, height / 2)
        }
        var loc = checkArea(predicate, tile.plane + 1..3, tile, radius)
        if (loc == null) {
            loc = checkArea(predicate, tile.plane - 1 downTo 0, tile, radius)
        }
        if (loc == null) {
            loc = checkArea({
                val def = objectDecoder.get(it.id)
                it != obj && (def.climb() || def.climb(true) || def.climb(true))
            }, tile.plane..tile.plane, tile, 2)
        }
        return loc
    }

    private fun checkArea(predicate: (GameObject) -> Boolean, planes: IntProgression, tile: Tile, radius: Int): GameObject? {
        for (p in planes) {
            Spiral.spiral(tile.copy(y = tile.y, plane = p), radius) { t ->
                uncertainty++
                val found = map[t]?.firstOrNull(predicate)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }

    private fun ObjectDefinition.isTrapdoor() = options[0].equals("open", true) && (name.contains("trap", true) || name.equals("manhole", true))

    private fun ObjectDefinition.climb(up: Boolean) = getOption(up) != null

    private fun ObjectDefinition.climb() = getOption() != null

    private fun ObjectDefinition.getOption(up: Boolean) = options.firstOrNull { it.equals("climb-${if (up) "up" else "down"}", true) || it.equals("climb ${if (up) "up" else "down"}", true) }

    private fun ObjectDefinition.getOption() = options.firstOrNull { it?.startsWith("climb", true) == true }

    companion object {
        private const val dungeonOffset = 6400
    }

}