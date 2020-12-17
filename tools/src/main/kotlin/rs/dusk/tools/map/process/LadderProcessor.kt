package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.update.task.viewport.Spiral
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.map.view.graph.GraphIO
import rs.dusk.tools.map.view.graph.NavigationGraph
import kotlin.math.max

class LadderProcessor(
    tileDecoder: TileDecoder,
    mapDecoder: GameObjectMapDecoder,
    private val objectDecoder: ObjectDecoder,
    xteas: Xteas,
    cache: Cache
) : ObjectProcessor(tileDecoder, mapDecoder, xteas, cache) {
    var count = 0
    val map = mutableMapOf<Tile, MutableList<GameObjectLoc>>()

    override fun process(region: Region, objects: List<GameObjectLoc>) {
        objects.forEach {
            map.getOrPut(Tile(it.x, it.y, it.plane)) {
                mutableListOf()
            }.add(it)
        }
    }

    var uncertainty = 0

    val upPredicate: (GameObjectLoc) -> Boolean = {
        val def = objectDecoder.get(it.id)
        def.climb(false) || def.options[0]?.equals("open", true) == true
    }
    val downPredicate: (GameObjectLoc) -> Boolean = {
        objectDecoder.get(it.id).climb(true)
    }
    val predicate: (GameObjectLoc) -> Boolean = {
        val def = objectDecoder.get(it.id)
        def.climb()
    }

    fun collect(tile: Tile, radius: Int): List<GameObjectLoc> {
        val list = mutableListOf<GameObjectLoc>()
        Spiral.spiral(tile, radius) { t ->
            list.addAll(map[t] ?: return@spiral)
        }
        return list
    }

    fun getFirstOption(id: Int): String? {
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

    fun finish() {
        val graph = NavigationGraph()
        GraphIO(graph, "./worldmaplinks.json").load()
        val interactable: (GameObjectLoc) -> Boolean = {
            getFirstOption(it.id) != null
        }
        // TODO combine with MapLinkDumper
        val it = graph.links.iterator()
        while (it.hasNext()) {
            val link = it.next()
            val start = collect(Tile(link.x, link.y, link.z), 4).filter(interactable)
            if (start.isNotEmpty()) {
                val loc = start.first()
                link.actions = mutableListOf("object ${loc.id} ${getFirstOption(loc.id)}")
            } else {
                it.remove()
                println("Unknown $link")
            }
        }
        graph.changed = true
        val unknowns = mutableSetOf<GameObjectLoc>()
        map.forEach { (tile, objs) ->
            objs@ for (obj in objs) {
                val def = objectDecoder.get(obj.id)

                var width = def.sizeX
                var height = def.sizeY

                if (obj.rotation and 0x1 == 1) {
                    width = def.sizeY
                    height = def.sizeX
                }

                if (def.climb(true)) {
                    val loc = checkForLadder(tile, width, height, true)
                    if (loc != null) {
                        link(graph, obj, tile, loc, true)
                    } else {
                        unknowns.add(obj)
                    }
                }

                if (def.climb(false)) {
                    val loc = checkForLadder(tile, width, height, false)
                    if (loc != null) {
                        link(graph, obj, tile, loc, false)
                    } else {
                        unknowns.add(obj)
                    }
                }


                if (!def.climb(true) && !def.climb(false) && def.climb()) {
                    val loc = checkForClimb(obj, tile, width, height)
                    if (loc != null) {
                        var link = graph.createLink(tile.x, tile.y, tile.plane, loc.x, loc.y, loc.plane)
                        link.actions = mutableListOf("object ${obj.id} ${objectDecoder.get(obj.id).getOption()}")
                        link = graph.createLink(loc.x, loc.y, loc.plane, tile.x, tile.y, tile.plane)
                        link.actions = mutableListOf("object ${loc.id} ${objectDecoder.get(loc.id).getOption()}")
                    } else {
                        println("Unknown ${obj.id} ${objectDecoder.get(obj.id).name} ${tile.x}, ${tile.y}, ${tile.plane}")
                        unknowns.add(obj)
                    }
                }
            }
        }
        unknowns.forEach {
            val link = graph.getLinkOrNull(it.x, it.y, it.plane)
            if (link == null) {
//                println("Unknown ${it.id} ${objectDecoder.get(it.id).name} ${it.x}, ${it.y}, ${it.plane}")
                count++
            }
        }
        graph.changed = true
        GraphIO(graph, "./ladders.json").save()
        println("Found ${graph.links.size} unknown $count")
    }

    private fun link(graph: NavigationGraph, obj: GameObjectLoc, tile: Tile, loc: GameObjectLoc, up: Boolean) {
        // TODO use collision and canReach to determine proper offset
        var link = graph.createLink(tile.x, tile.y, tile.plane, loc.x, loc.y, loc.plane)
        link.actions = mutableListOf("object ${obj.id} ${objectDecoder.get(obj.id).getOption(up)}")
        link = graph.createLink(loc.x, loc.y, loc.plane, tile.x, tile.y, tile.plane)
        link.actions = mutableListOf("object ${loc.id} ${objectDecoder.get(loc.id).getOption(!up)}")
    }

    private fun checkForLadder(tile: Tile, width: Int, height: Int, up: Boolean): GameObjectLoc? {
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

    private fun checkForClimb(obj: GameObjectLoc, tile: Tile, width: Int, height: Int): GameObjectLoc? {
        var tile = tile
        uncertainty = 0
        var radius = 4//max(width, height)
        if (radius > 1) {
            tile = tile.add(width / 2, height / 2)
//            radius *= 2
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

    private fun checkArea(predicate: (GameObjectLoc) -> Boolean, planes: IntProgression, tile: Tile, radius: Int): GameObjectLoc? {
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

    fun ObjectDefinition.climb(up: Boolean) = getOption(up) != null

    fun ObjectDefinition.climb() = getOption() != null

    fun ObjectDefinition.getOption(up: Boolean) = options.firstOrNull { it.equals("climb-${if (up) "up" else "down"}", true) || it.equals("climb ${if (up) "up" else "down"}", true) }

    fun ObjectDefinition.getOption() = options.firstOrNull { it?.startsWith("climb", true) == true }

    companion object {
        private const val dungeonOffset = 6400
    }

}