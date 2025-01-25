package content.area.kandarin.tree_gnome_stronghold

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace

objectOperate("Open", "tree_gnome_door_east_closed", "tree_gnome_door_west_closed") {
    if (player.tile.x !in 2465..2466) {
        player.walkToDelay(player.tile.copy(x = player.tile.x.coerceIn(2465..2466)))
        delay()
        player.face(target)
    }
    target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
    player.walkOverDelay(player.tile.addY(if (player.tile.y < target.tile.y) 2 else -2))
}