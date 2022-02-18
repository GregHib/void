package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import java.util.*

class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val waypoints: LinkedList<Edge> = LinkedList()
) {

    var path: Path = Path.EMPTY
        private set

    fun step(direction: Direction, run: Boolean) {
        if (run) {
            runStep = direction
        } else {
            walkStep = direction
        }
    }

    fun set(strategy: TileTargetStrategy, type: PathType = PathType.Dumb, ignore: Boolean = false) {
        clear()
        this.path = Path(strategy, type, ignore)
    }

    fun clearPath() {
        waypoints.clear()
        path = Path.EMPTY
    }

    fun clear() {
        clearPath()
        reset()
    }

    fun reset() {
        delta = Delta.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

var Character.running: Boolean
    get() = get("running", false)
    set(value) = set("running", value)

var Character.moving: Boolean
    get() = get("moving", false)
    set(value) = set("moving", value)