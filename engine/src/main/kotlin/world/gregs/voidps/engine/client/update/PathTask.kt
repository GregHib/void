package world.gregs.voidps.engine.client.update

import org.rsmod.pathfinder.RouteCoordinates
import org.rsmod.pathfinder.SmartPathFinder
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.event.MoveStop
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.tick.delay

/**
 * Calculates paths for characters that want to move
 */
class PathTask<C : Character>(
    iterator: TaskIterator<C>,
    private val collisions: Collisions,
    override val characters: CharacterList<C>,
    private val finder: PathFinder
) : CharacterTask<C>(iterator) {

    val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = true)

    override fun predicate(character: C): Boolean {
        return character.movement.path.state == Path.State.Waiting
    }

    override fun run(character: C) {
        val path = character.movement.path
        if (character is Player) {
            val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = true)
            val route = pf.findPath(character.tile.x, character.tile.y, path.strategy.tile.x, path.strategy.tile.y, character.tile.plane)
            println(route)
            if (route.alternative) {
                path.result = PathResult.Partial(route.last().toTile(character.tile.plane))
            } else if (route.success) {
                path.result = PathResult.Success(route.last().toTile(character.tile.plane))
            } else if (route.failed) {
                path.result = PathResult.Failure
            }
            var index = 0
            for (coordinate in route.coords) {
                character.delay(++index) {
                    character.tele(coordinate.toTile(character.tile.plane))
                }
            }
        } else {
            path.result = finder.find(character, path, path.type, path.ignore)
        }
        if (path.result is PathResult.Failure || (path.result is PathResult.Partial && path.steps.isEmpty())) {
            character.events.emit(MoveStop)
        }
    }
}

fun RouteCoordinates.toTile(plane: Int) = Tile(x, y, plane)