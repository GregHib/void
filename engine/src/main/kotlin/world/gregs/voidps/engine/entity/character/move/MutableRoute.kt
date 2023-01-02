package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.Route
import org.rsmod.pathfinder.RouteCoordinates
import java.util.*

data class MutableRoute(
    val steps: LinkedList<RouteCoordinates>,
    val partial: Boolean,
    val success: Boolean
) {
    val failed: Boolean
        get() = !success
    companion object {
        val EMPTY = MutableRoute(LinkedList(), false, false)
    }
}

fun Route.toMutableRoute() = MutableRoute(LinkedList(coords), alternative, success)