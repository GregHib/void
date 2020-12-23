package rs.dusk.tools.map.process

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

class LadderLinker(
    val graph: NavigationGraph,
    private val objectDecoder: ObjectDecoder,
    private val linker: ObjectLinker
) : Pipeline.Modifier<Map<Tile, List<GameObject>>> {

    private lateinit var map: Map<Tile, List<GameObject>>
    var count = 0

    var uncertainty = 0


    /*
    TODO BETTER COMPARING SYSTEM
        Valid objects should be interactive and Have to have a name
        TODO compare names of matches
            neither are null
            equals ignore case
            or both contains stair
            or one contains ladder and the other trapdoor or manhole
        TODO invalid object if surrounding area (radius 2) has all clipping as 0 (should remove stronghold + gwd rope issues)
     */
    /*
        Is bidirectional linking even needed if filtering is functional?
        Use models to compare too?

        fun process(obj1) {
            process all valid objects with same name category in correct places
        }

        fun process(obj1, targetTile) {
            process all valid objects near targetTile
        }

        fun process(obj1, obj2) {
            return tile1, tile2, obj1, obj1Option
        }
     */
    private val upPredicate: (GameObject) -> Boolean = {
        val def = objectDecoder.get(it.id)
        (def.climb(false) || def.isTrapdoor()) && linker.isReachable(it)
    }

    private val downPredicate: (GameObject) -> Boolean = {
        objectDecoder.get(it.id).climb(true) && linker.isReachable(it)
    }

    private val predicate: (GameObject) -> Boolean = {
        val def = objectDecoder.get(it.id)
        def.climb() && linker.isReachable(it)
    }

    override fun process(content: Map<Tile, List<GameObject>>) {
        val start = graph.links.size
        map = content
        val unknowns = mutableSetOf<GameObject>()
        content.forEach { (tile, objs) ->
            objs@ for (obj in objs) {
                val def = obj.def

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
                }
            }
        }
        unknowns.forEach { obj ->
//            if (!graph.contains(obj.tile)) {
                when {
                    obj.def.name == "Dungeon exit" -> {
                        val tile = linker.getAvailableTiles(obj).first()
                        val link = graph.addLink(tile, Tile(3460, 3721, 1))
                        link.actions = mutableListOf("object ${obj.id} ${obj.def.options.first()!!}")
                        graph.track(obj, obj.def.options.first(), link)
                    }
                    else -> {
                        println("Unknown ${obj.id} ${objectDecoder.get(obj.id).name} ${obj.tile}")
                        count++
                    }
                }
//            }
        }
        println("Found ${graph.links.size - start} unknown $count")
    }

    private fun link(unknowns: MutableSet<GameObject>, graph: NavigationGraph, obj: GameObject, loc: GameObject?, type: Int) {
        if (loc == null) {
            unknowns.add(obj)
            return
        }
        val output = linker.linkedPoints(obj, loc)
        if (output == null) {
            unknowns.add(obj)
        } else {
            val start = output.first
            val end = output.second
            if(obj.def.name != loc.def.name) {
                println("Name mismatch $obj ${obj.def.name} ${loc.def.name}")
            }
            var link = graph.addLink(start, end)
            var option = when (type) {
                0 -> objectDecoder.get(obj.id).getOption(false)
                1 -> objectDecoder.get(obj.id).getOption(true)
                else -> objectDecoder.get(obj.id).getOption()
            }
            link.actions = mutableListOf("object ${obj.id} $option")
            graph.track(obj, option, link)
            link = graph.addLink(end, start)
            option = when (type) {
                0 -> objectDecoder.get(loc.id).getOption(true)
                1 -> objectDecoder.get(loc.id).getOption(false)
                else -> objectDecoder.get(loc.id).getOption()
            }
            link.actions = mutableListOf("object ${loc.id} $option")
            graph.track(loc, option, link)
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