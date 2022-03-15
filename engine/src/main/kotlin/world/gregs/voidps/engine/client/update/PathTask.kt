package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.event.MoveStop
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult

/**
 * Calculates paths for characters that want to move
 */
class PathTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>,
    private val finder: PathFinder
) : CharacterTask<C>(iterator) {

    override fun predicate(character: C): Boolean {
        return character.movement.path.state == Path.State.Waiting
    }

    override fun run(character: C) {
        val path = character.movement.path
        path.result = finder.find(character, path, path.type, path.ignore)
        if (path.result is PathResult.Failure || (path.result is PathResult.Partial && path.steps.isEmpty())) {
            character.events.emit(MoveStop)
        }
    }
}