package content.area.wilderness

import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.equals

class WildernessWall : Script {

    init {
        objectOperate("Cross", "wilderness_wall_*") { (target) ->
            if (target.id == "wilderness_wall_1" && target.tile.equals(2996, 3531)) {
                val direction = if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
                anim("wild_ditch_jump")
                exactMoveDelay(tile.copy(x = target.tile.x + if (direction == Direction.EAST) 2 else -1), delay = 60, direction = direction)
                return@objectOperate
            }
            val direction = if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            anim("wild_ditch_jump")
            exactMoveDelay(tile.copy(y = target.tile.y + if (direction == Direction.NORTH) 2 else -1), delay = 60, direction = direction)
        }
    }
}
