package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import java.util.*

class Path(
    val strategy: TileTargetStrategy,
    val callback: ((Path) -> Unit)? = null
) {
    val steps = LinkedList<Direction>()
    var result: PathResult? = null
        set(value) {
            field = value
            state = State.Progressing
        }
    var state: State = State.Waiting

    sealed class State {
        /**
         * Awaiting steps to be calculated
         */
        object Waiting : State()

        /**
         * Steps have been calculated and are being followed out
         */
        object Progressing : State()

        /**
         * Steps have been made and callback invoked
         */
        object Complete : State()
    }

    companion object {
        val EMPTY = Path(SingleTileTargetStrategy(Tile.EMPTY)).apply {
            state = State.Complete
        }
    }
}