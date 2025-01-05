package world.gregs.voidps.world.map.tree_gnome_stronghold

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.queue.strongQueue

objectOperate("Open", "tree_gnome_door_east_closed", "tree_gnome_door_west_closed") {
    player.start("input_delay", 4)
    player.strongQueue("enter_tree_gnome_door") {
        if (player.tile.x !in 2465..2466) {
            player.walkTo(player.tile.copy(x = player.tile.x.coerceIn(2465..2466)))
            pause(1)
            player.face(target)
        }
        player.walkTo(player.tile.addY(if (player.tile.y < target.tile.y) 2 else -2), noCollision = true, noRun = true)
        target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
    }
}