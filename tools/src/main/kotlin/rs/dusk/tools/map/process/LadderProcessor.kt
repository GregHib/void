package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.map.view.graph.GraphIO
import rs.dusk.tools.map.view.graph.NavigationGraph

class LadderProcessor(
    tileDecoder: TileDecoder,
    mapDecoder: GameObjectMapDecoder,
    private val objectDecoder: ObjectDecoder,
    xteas: Xteas,
    cache: Cache
) : ObjectProcessor(tileDecoder, mapDecoder, xteas, cache) {
    var count = 0
    val map = mutableMapOf<Tile, MutableList<GameObjectLoc>>()
    val links = mutableMapOf<GameObjectLoc, GameObjectLoc>()

    override fun process(region: Region, objects: List<GameObjectLoc>) {
        objects.forEach {
            map.getOrPut(Tile(it.x, it.y, it.plane)) {
                mutableListOf()
            }.add(it)
        }
    }

    fun finish() {
        val graph = NavigationGraph()
        /*
            TODO "Climb" & staircases
         */
        val unknowns = mutableSetOf<GameObjectLoc>()
        map.forEach { (tile, objs) ->
            objs@ for (obj in objs) {
                val def = objectDecoder.get(obj.id)

                val loc = when {
                    def.climbUp() -> checkForLink(tile, true)
                    def.climbDown() -> checkForLink(tile, false)
                    else -> continue@objs
                }
                if (loc != null) {
                    graph.createLink(tile.x, tile.y, tile.plane, loc.x, loc.y, loc.plane)
                } else {
                    unknowns.add(obj)
                }
            }
        }
        unknowns.forEach {
            val node = graph.getNodeOrNull(it.x, it.y, it.plane)
            if (node == null) {
                println("Unknown ${it.id} ${objectDecoder.get(it.id).name} ${it.x}, ${it.y}, ${it.plane}")
                count++
            }
        }
        GraphIO(graph, "./ladders.json").save()
    }

    private fun checkForLink(tile: Tile, up: Boolean): GameObjectLoc? {
        val offset = 6400
        var found: GameObjectLoc? = null
        val planes = if (up) tile.plane + 1..3 else 0..tile.plane
        val inOppositeArea = if (up) tile.y > offset else tile.y < offset
        // Height
        if (found == null) {
            plane@ for (p in planes) {
                found = check(map[tile.copy(plane = p)], up)
                if (found != null) {
                    break@plane
                }
            }
        }
        // Surface
        if (found == null && inOppositeArea) {
            found = check(map[tile.add(0, if (up) -offset else offset, 0)], up)
        }
        // Surrounding height
        if (found == null) {
            plane@ for (p in planes) {
                for (dir in Direction.all) {
                    found = check(map[tile.add(dir.delta.x, dir.delta.y, p)], up)
                    if (found != null) {
                        break@plane
                    }
                }
            }
        }
        // Surface surrounding
        if (found == null && inOppositeArea) {
            plane@ for (dir in Direction.all) {
                found = check(map[tile.add(dir.delta.x, dir.delta.y + if (up) -offset else offset, 0)], up)
                if (found != null) {
                    break@plane
                }
            }
        }
        // Wider surrounding
        if (found == null) {
            plane@ for (p in planes) {
                for (dir in Direction.cardinal) {
                    found = check(map[tile.add(dir.delta.x * 2, dir.delta.y * 2, p)], up)
                    if (found != null) {
                        break@plane
                    }
                }
            }
        }
        return found
    }

    fun check(objects: List<GameObjectLoc>?, up: Boolean): GameObjectLoc? {
        return objects?.firstOrNull {
            val def = objectDecoder.get(it.id)
            if (up) {
                def.climbDown()
            } else {
                def.climbUp()
            }
        }
    }

    fun ObjectDefinition.climbUp() = options[0]?.equals("climb-up", true) == true || options[0]?.equals("climb up", true) == true

    fun ObjectDefinition.climbDown() = options[0]?.equals("climb-down", true) == true || options[0]?.equals("climb down", true) == true
}