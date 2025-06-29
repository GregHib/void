package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction

objectOperate("Move", "godwars_boulder") {
    player.walkToDelay(target.tile.addY(-1))
    player.face(Direction.NORTH)
    delay(2)
    player.anim("godwars_move_boulder")
    delay(2)
    target.anim("6985")
}