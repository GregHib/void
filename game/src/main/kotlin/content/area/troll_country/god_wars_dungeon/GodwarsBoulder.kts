package content.area.troll_country.god_wars_dungeon

import content.entity.obj.objTeleportLand
import content.entity.sound.areaSound
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction

objectOperate("Move", "godwars_boulder") {
    val direction = if (player.tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
    player.walkToDelay(target.tile.addY(if (direction == Direction.NORTH) -1 else 4))
    player.face(direction)
    delay(2)
    player.anim("godwars_move_boulder${if (direction == Direction.NORTH) "" else "_reverse"}")
    delay(2)
    target.anim("godwars_boulder_move")
    areaSound("godwars_move_boulder", target.tile, radius = 5)
    delay(3)
    player.exactMoveDelay(target.tile.addY(if (direction == Direction.NORTH) 4 else -1), delay = 210, direction = direction)
    delay(5)
    areaSound("godwars_boulder_rollback", target.tile, radius = 5)
    target.anim("godwars_boulder_rollback")
}
