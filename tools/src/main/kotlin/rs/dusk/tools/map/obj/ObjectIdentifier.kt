package rs.dusk.tools.map.obj

import rs.dusk.ai.DecisionMaker
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.Objects
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
        val interactiveOptions = interactiveObjects.flatMap { obj -> obj.getOptions() }

        /**
         *  TODO
         *      Take size into consideration with distance checks; invalid matches like cw stairs shouldn't be getting as high as 0.875 (36481, tile=Tile(x=2417, y=3077, plane=0)
         *      Add +6400 (if coords are correct take min of both calculations)
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
                    interactionScore,
                    ladderDistance,
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
                    interactionScore,
                    stairDistance,
                    stairOptionNameOpposition,
                    differenceBetweenIds
                )
            )
        )

        val climbables =
            interactiveObjects
                .filter { it.def.options.firstOrNull()?.replace("-", " ").equals("climb up", true) }
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
        return def.options.filterNotNull().filterNot { it == "Examine" }.map { GameObjectOption(it, this, tiles) }
    }
}