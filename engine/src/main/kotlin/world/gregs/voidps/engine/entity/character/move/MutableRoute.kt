package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.Route
import org.rsmod.pathfinder.RouteCoordinates
import java.util.*

data class MutableRoute(
    val coords: LinkedList<RouteCoordinates>,
    val alternative: Boolean,
    val success: Boolean
) {
    val failed: Boolean
        get() = !success
}

fun Route.toMutableRoute() = MutableRoute(LinkedList(coords), alternative, success)