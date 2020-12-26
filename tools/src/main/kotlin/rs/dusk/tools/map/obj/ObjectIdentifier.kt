package rs.dusk.tools.map.obj

import rs.dusk.ai.DecisionMaker
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.map.Tile
import rs.dusk.tools.map.process.ObjectLinker
import rs.dusk.utility.get

class ObjectIdentifier(private val linker: ObjectLinker) {

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
//            .filter { it.obj.id == 2446 && it.obj.tile.equals(2463, 3497) }

        /**
         *  TODO
         *      Add cache links data
         *      Add non-plane-changing if two objects are touching
         *      Add non-plane-changing for single objects
         *      Add doors
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
            )
        )

        val climbables =
            interactiveObjects
                .filter { it.def.options.firstOrNull()?.replace("-", " ").equals("climb up", true)
//                        && it.id == 32015 && it.tile.equals(2463, 9897)
                }
        climbables.forEach { obj ->
            for (opt in obj.getOptions()) {
                val context = ObjectIdentificationContext(obj, opt.tiles, opt.option)
                val decision = decisionMaker.decide(context, options)
                if (decision != null) {
                    println("${decision.score} $obj ${opt.option} decision ${(decision.option as ObjectIdentification).name} ${decision.target}")
                }
            }
        }

        println("${objects.size} objects ${interactiveObjects.size} interactive ${climbables.size} climbable")
        var count = 0
        /*
            opposites
            climb-up climb-down/open/jump-down
            enter exit/exit-through/exit-room/exit-cave
            open close/leave/leave tomb/

         */
    }

    private fun GameObject.getOptions(): List<GameObjectOption> {
        val tiles = linker.getTiles(this)
        val openId = def.getOrNull("open") as? Int
        if(openId != null) {
            val openObj = GameObject(openId, tile, type, rotation, owner)
            return openObj.options(tiles) + options(tiles)
        }
        return options(tiles)
    }

    private fun GameObject.options(tiles: Set<Tile>): List<GameObjectOption> {
        return def.options.filterNotNull().filterNot { it == "Examine" }.map { GameObjectOption(it, this, tiles) }
    }
}