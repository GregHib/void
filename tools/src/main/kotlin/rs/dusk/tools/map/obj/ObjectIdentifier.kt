package rs.dusk.tools.map.obj

import rs.dusk.ai.DecisionMaker
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.map.Tile
import rs.dusk.tools.map.process.ObjectLinker
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.utility.get

class ObjectIdentifier(private val linker: ObjectLinker, private val worldMapLinks: List<Pair<Tile, Tile>>, val graph: NavigationGraph) {

    val decisionMaker = DecisionMaker()
    val objs = get<Objects>()

    /**
     * Ignore re-used objects, e.g chains on planes > 0 in stronghold of security
     */
    private fun isReused(obj: GameObject): Boolean {
        if (obj.tile.plane > 0) {
            if (objs[obj.tile.add(plane = 1)].any { it.id == obj.id }) {
                return false
            }
            if (objs[obj.tile.minus(plane = 1)].any { it.id == obj.id }) {
                return false
            }
        }
        return true
    }

    fun compare(objects: List<GameObject>) {
        val interactiveObjects = objects.filter {
            it.def.options.first() != null &&
                    linker.getAvailableTiles(it).isNotEmpty() &&
                    isReused(it)
        }
        val interactiveOptions = interactiveObjects
            .flatMap { obj -> obj.getOptions() }

        /**
         *  TODO
         *      Fix castle wars/side by side stairs issue
         *      Unit tests to make sure things are still balanced correct e.g dungeoneering exits, ladders side by side, castle wars stairs
         *      Dung exits shouldn't score as high as 0.56 for walls - Make "climb" only for same plane, "climb-up" and same plane should be lower
         *      Self-interactable
         *      Doors
         *      Shortcuts
         */
        val options = setOf(
            ObjectIdentification(
                "Ladder",
                { interactiveOptions.minus(obj.getOptions()) },
                setOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    ladderOptionNameOpposition,
                    differenceBetweenIds
                )
            ),
            ObjectIdentification(
                "Stairs",
                { interactiveOptions.minus(obj.getOptions()) },
                setOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    stairOptionNameOpposition,
                    differenceBetweenIds
                )
            ),
            ObjectIdentification(
                "World Map Link",
                { worldMapLinks },
                setOf(
                    distanceToTile
                )
            ),
            ObjectIdentification(
                "Wall",// Or one way ladder
                { linker.getAllTiles(obj) },
                setOf(
                    wallOptions,
                    isOppositeTile,
                    isPopulatedPlane
                )
            )
        )

        val climbables =
            interactiveObjects
                .filter {
                    val option = it.def.options.firstOrNull()?.replace("-", " ")?.toLowerCase()
                    option == "climb up" || option == "climb down"
                }

        if (false) {
            val debug = climbables.firstOrNull { it.id == 36521 && it.tile.equals(2371, 3126, 1) }
            val target = interactiveOptions.firstOrNull { it.obj.id == 36694 && it.obj.tile.equals(2369, 3125) }
            val target2 = interactiveOptions.firstOrNull { it.obj.id == 4415 && it.obj.tile.equals(2369, 3126, 2) }
            if (debug != null && target != null && target2 != null) {
                for(opt in debug.getOptions()) {
                    val context = ObjectIdentificationContext(debug, opt.tiles, opt.option)
                    println(DecisionMaker().decide(context, options))
                    val opt = options.first() as ObjectIdentification<GameObjectOption>
                    println(opt.getScores(context, target))
//                    println(opt.getHighestTarget(context, 0.0))
                    val opt2 = options.first() as ObjectIdentification<GameObjectOption>
                    println(opt2.getScores(context, target2))
//                    println(opt2.getHighestTarget(context, 0.0))
                }
            }
            System.exit(0)
        }

        var found = 0
        var unknown = 0
        var n = 0
        climbables.forEach { obj ->
            for (opt in obj.getOptions()) {
                val context = ObjectIdentificationContext(obj, opt.tiles, opt.option)
                val decision = decisionMaker.decide(context, options)
                if (decision != null) {
                    if (decision.score > 0.5) {
                        val target = decision.target
                        if (target is GameObjectOption) {
                            graph.addLink(context.obj.tile, target.obj.tile)
                        } else if(target is Pair<*, *>) {
                            graph.addLink(context.obj.tile, target.second as Tile)
                        }
                        found++
                    } else {
                        unknown++
                    }
                    println("${decision.score} $obj ${opt.option} decision ${(decision.option as ObjectIdentification).name} ${decision.target}")
                } else {
                    n++
                    println("No decision found $opt")
                }
            }
        }

        //2399 identified 277 unknown
        println("${objects.size} objects ${interactiveObjects.size} interactive $found identified $unknown unknown $n null")
    }

    private fun GameObject.getOptions(): List<GameObjectOption> {
        val tiles = linker.getTiles(this)
        val openId = def.getOrNull("open") as? Int
        if (openId != null) {
            val openObj = GameObject(openId, tile, type, rotation, owner)
            return openObj.options(tiles) + options(tiles)
        }
        return options(tiles)
    }

    private fun GameObject.options(tiles: Set<Tile>): List<GameObjectOption> {
        return def.options.filterNotNull().filterNot { it == "Examine" }.map { GameObjectOption(it, this, tiles) }
    }
}