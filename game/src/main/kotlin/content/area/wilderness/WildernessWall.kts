package content.area.wilderness

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction

objectOperate("Cross", "wilderness_wall_*") {
    val direction = if (player.tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
    player.anim("wild_ditch_jump")
    player.exactMoveDelay(player.tile.copy(y = target.tile.y + if (direction == Direction.NORTH) 2 else -1), delay = 60, direction = direction)
}