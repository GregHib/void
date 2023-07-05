package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.Route
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.type.Tile
import java.util.*

class Steps(
    internal val character: Character,
    val steps: LinkedList<Tile> = LinkedList<Tile>()
) : List<Tile> by steps {
    var destination: Tile = Tile.EMPTY
        private set

    fun peek(): Tile? = steps.peek()

    fun poll(): Tile = steps.poll()

    fun queueRoute(route: Route, target: Tile? = null) {
        queueSteps(route.waypoints.map { character.tile.copy(it.x, it.z) })
        destination = target ?: steps.lastOrNull() ?: character.tile
    }

    fun queueStep(tile: Tile) {
        clear()
        steps.add(tile)
        destination = tile
    }

    fun queueSteps(tiles: List<Tile>) {
        clear()
        steps.addAll(tiles)
        destination = tiles.lastOrNull() ?: character.tile
    }

    fun clearDestination() {
        destination = Tile.EMPTY
    }

    fun clear() {
        steps.clear()
        clearDestination()
    }
}