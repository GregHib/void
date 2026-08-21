package content.area.morytania.port_phasmatys

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

/**
 * The barrier transforms once Ghosts Ahoy is complete; before then it offers a toll of two
 * ecto-tokens, afterwards it can only be passed and is always free.
 */
class PortPhasmatysBarrier : Script {

    init {
        objectOperate("Pass", "phasmatys_barrier_north") { (target) ->
//            https://youtu.be/PrkWAZmuEnw?si=T86lk1tMR91q2fjv&t=150
            message("All visitors to Port Phasmatys must pay a toll charge of 2 Ectotokens. However, you have done the ghosts of our town a service that surpasses all values, so you may pass without charge.", ChatType.Filter)
            cross(target)
        }

        objectOperate("Pass", "phasmatys_barrier") { (target) ->
            if (!leaving()) {
                message("All visitors to Port Phasmatys must pay a toll charge of 2 Ectotokens.", ChatType.Filter)
                return@objectOperate
            }
            cross(target)
        }

        objectOperate("Pay-toll(2-Ecto)", "phasmatys_barrier") { (target) ->
            if (leaving()) {
                cross(target)
                return@objectOperate
            }
            if (!inventory.remove("ecto_token", TOLL)) {
                message("All visitors to Port Phasmatys must pay a toll charge of 2 Ectotokens.", ChatType.Filter)
                message("You need to go to the Temple and earn some. Talk to the disciples - they will tell you how.", ChatType.Filter)
                return@objectOperate
            }
            message("You pay the toll charge of 2 Ectotokens.", ChatType.Filter)
            cross(target)
        }
    }

    /** The toll is only charged on the way in; leaving the city is always free. */
    private fun Player.leaving(): Boolean = tile in Areas["port_phasmatys"]

    private suspend fun Player.cross(target: GameObject) {
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

    companion object {
        private const val TOLL = 2
    }
}
