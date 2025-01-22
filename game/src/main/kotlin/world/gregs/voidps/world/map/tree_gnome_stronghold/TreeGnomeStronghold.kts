package world.gregs.voidps.world.map.tree_gnome_stronghold

import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.walkOver
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace

objectOperate("Open", "tree_gnome_door_east_closed", "tree_gnome_door_west_closed") {
    if (player.tile.x !in 2465..2466) {
        player.walkTo(player.tile.copy(x = player.tile.x.coerceIn(2465..2466)))
        delay()
        player.face(target)
    }
    target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
    player.walkOver(player.tile.addY(if (player.tile.y < target.tile.y) 2 else -2))
}