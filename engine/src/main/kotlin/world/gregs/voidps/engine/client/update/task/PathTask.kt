package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Calculates paths for characters that want to move
 */
class PathTask<C : Character>(
    override val entities: PooledMapList<C>,
    private val finder: PathFinder
) : EntityTask<C>() {

    override fun predicate(entity: C): Boolean {
        return entity.movement.path.state == Path.State.Waiting
    }

    override fun runAsync(entity: C) {
        val path = entity.movement.path
        path.result = finder.find(entity, path, path.type, path.ignore)
        if (path.result is PathResult.Failure || (path.result is PathResult.Partial && path.steps.isEmpty())) {
            entity.events.emit(MoveStop)
        }
    }
}