package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.Route
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.type.Tile
import java.util.*

class Steps(
    internal val character: Character,
    val steps: LinkedList<Step> = LinkedList<Step>()
) : List<Step> by steps {
    var destination: Tile = Tile.EMPTY
        private set

    fun peek(): Step? = steps.peek()

    fun poll(): Step = steps.poll()

    fun queueRoute(route: Route, target: Tile? = null, noCollision: Boolean = false, slowRun: Boolean = false) {
        queueSteps(route.waypoints.map { character.tile.copy(it.x, it.z) }, noCollision, slowRun)
        destination = (target ?: steps.lastOrNull() ?: character.tile).step(noCollision, slowRun)
    }

    fun queueStep(tile: Tile, noCollision: Boolean = false, slowRun: Boolean = false) {
        clear()
        steps.add(tile.step(noCollision, slowRun))
        destination = tile.step(noCollision, slowRun)
    }

    fun queueSteps(tiles: List<Tile>, noCollision: Boolean = false, slowRun: Boolean = false) {
        clear()
        steps.addAll(tiles.map { it.step(noCollision, slowRun) })
        destination = steps.lastOrNull() ?: character.tile.step(noCollision, slowRun)
    }

    fun clearDestination() {
        destination = Tile.EMPTY
    }

    fun clear() {
        steps.clear()
        clearDestination()
    }
}