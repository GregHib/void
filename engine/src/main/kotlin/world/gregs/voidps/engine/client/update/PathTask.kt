package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.event.MoveStop
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.toMutableRoute
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Calculates paths for characters that want to move
 */
class PathTask<C : Character>(
    iterator: TaskIterator<C>,
    collisions: Collisions,
    override val characters: CharacterList<C>
) : CharacterTask<C>(iterator) {

    private val pf = org.rsmod.pathfinder.PathFinder(flags = collisions.data, useRouteBlockerFlags = true)

    override fun predicate(character: C): Boolean {
        return character.movement.path.state == Path.State.Waiting
    }

    override fun run(character: C) {
        val path = character.movement.path
        val route = pf.findPath(
            character.tile.x,
            character.tile.y,
            path.strategy.tile.x,
            path.strategy.tile.y,
            character.tile.plane,
            srcSize = character.size.width,
            destWidth = path.strategy.size.width,
            destHeight = path.strategy.size.height,
            collision = character.collision
        ).toMutableRoute()
        character.movement.route = route
        if (route.failed) {
            character.events.emit(MoveStop)
        }
    }
}