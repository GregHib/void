package content.area.morytania.port_phasmatys

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Tile

@Script
class PortPhasmatysBarrier {
    init {
        objectOperate("Pass", "phasmatys_barrier") {
            if (target.rotation == 2) {
                val x = if (player.tile.x <= target.tile.x) target.tile.x + 1 else target.tile.x
                val y = player.tile.y.coerceIn(target.tile.y, target.tile.y + 1)
                player.walkOverDelay(player.tile.copy(y = y))
                player.walkOverDelay(Tile(x, y))
            } else if (target.rotation == 3) {
                val x = player.tile.x.coerceIn(target.tile.x, target.tile.x + 1)
                val y = if (player.tile.y >= target.tile.y) target.tile.y - 1 else target.tile.y
                player.walkOverDelay(player.tile.copy(x = x))
                player.walkOverDelay(Tile(x, y))
            }
        }
    }
}
