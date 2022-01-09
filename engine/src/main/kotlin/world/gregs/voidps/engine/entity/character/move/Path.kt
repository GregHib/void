package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import java.util.*

class Path(
    val strategy: TileTargetStrategy,
    val type: PathType = PathType.Dumb,
    val ignore: Boolean = true
) {
    val steps = LinkedList<Direction>()
    var result: PathResult? = null
    val state: State
        get() = when {
            result == null -> State.Waiting
            steps.isEmpty() -> State.Complete
            else -> State.Progressing
        }

    fun recalculate() {
        result = null
        steps.clear()
    }

    sealed class State {
        /**
         * Awaiting steps to be calculated
         */
        object Waiting : State()

        /**
         * Steps have been calculated and are being carried out
         */
        object Progressing : State()

        /**
         * Steps have been made and callback invoked
         */
        object Complete : State()
    }

    companion object {
        val EMPTY = Path(SingleTileTargetStrategy(Tile.EMPTY)).apply {
            result = PathResult.Success(Tile.EMPTY)
        }
    }
}