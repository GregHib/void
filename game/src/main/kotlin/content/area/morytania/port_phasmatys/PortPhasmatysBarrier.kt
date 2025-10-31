package content.area.morytania.port_phasmatys

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.Tile

class PortPhasmatysBarrier : Script {
    init {
        objectOperate("Pass", "phasmatys_barrier") { (target) ->
//            https://youtu.be/PrkWAZmuEnw?si=T86lk1tMR91q2fjv&t=150
            message("All visitors to Port Phasmatys must pay a toll charge of 2 Ectotockens. However, you have done the ghosts of our town a service that surpasses all values, so you may pass without charge.", ChatType.Filter)
            if (target.rotation == 2) {
                val x = if (tile.x <= target.tile.x) target.tile.x + 1 else target.tile.x
                val y = tile.y.coerceIn(target.tile.y, target.tile.y + 1)
                walkOverDelay(tile.copy(y = y))
                walkOverDelay(Tile(x, y))
            } else if (target.rotation == 3) {
                val x = tile.x.coerceIn(target.tile.x, target.tile.x + 1)
                val y = if (tile.y >= target.tile.y) target.tile.y - 1 else target.tile.y
                walkOverDelay(tile.copy(x = x))
                walkOverDelay(Tile(x, y))
            }
        }
    }
}
