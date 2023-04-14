package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.Route
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import java.util.*

class Steps(
    internal val character: Character,
    val steps: LinkedList<Tile> = LinkedList<Tile>()
) : List<Tile> by steps {
    internal var destination: Tile = Tile.EMPTY
    var forced: Boolean = false
    var partial: Boolean = false
        private set

    fun peek(): Tile? = steps.peek()

    fun poll(): Tile = steps.poll()

    fun queueRoute(route: Route, target: Tile? = null, forceMove: Boolean = false) {
        queueSteps(route.waypoints.map { character.tile.copy(it.x, it.z) }, forceMove)
        partial = route.alternative
        destination = target ?: steps.lastOrNull() ?: character.tile
    }

    fun queueStep(tile: Tile, forceMove: Boolean = false) {
        clear()
        forced = forceMove
        steps.add(tile)
        destination = tile
    }

    fun queueSteps(tiles: List<Tile>, forceMove: Boolean = false) {
        clear()
        forced = forceMove
        steps.addAll(tiles)
        destination = tiles.lastOrNull() ?: character.tile
    }

    fun clearDestination() {
        destination = Tile.EMPTY
    }

    fun clear() {
        steps.clear()
        clearDestination()
        partial = false
    }
}