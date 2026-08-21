package content.area.misthalin.zanaris.evil_chicken_lair

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class EvilChickenLair : Script {
    init {
        itemOnObjectOperate("raw_chicken", "chicken_shrine") {
            walkToDelay(SHRINE_TILE)
            if (!inventory.remove("raw_chicken")) {
                return@itemOnObjectOperate
            }
            message("You place the raw chicken on the shrine.")
            animDelay("teleport_evil_chicken_lair")
            tele(ENTRANCE)
            anim("teleport_land_evil_chicken_lair")
        }

        objTeleportTakeOff("Enter", "evil_chicken_lair_portal") { _, _ ->
            anim("teleport_evil_chicken_lair")
        }

        objTeleportLand("Enter", "evil_chicken_lair_portal") { _, _ ->
            anim("teleport_land_evil_chicken_lair")
        }

        itemOnObjectOperate("rope", "evil_chicken_lair_tunnel_entrance") { (target) ->
            arriveDelay()
            if (!inventory.remove("rope")) {
                return@itemOnObjectOperate
            }
            message("You tie the rope to the top of the tunnel and throw it down.")
            target.replace("evil_chicken_lair_tunnel_entrance_rope", ticks = ROPE_TICKS)
        }

        // The tunnel entrance isn't named like a rope so Stairs' generic climb animation doesn't apply.
        objTeleportTakeOff("Climb-down", "evil_chicken_lair_tunnel_entrance_rope") { _, _ ->
            val remaining = remaining("teleport_delay")
            if (remaining > 0) {
                return@objTeleportTakeOff remaining
            } else if (remaining < 0) {
                anim("climb_down")
                start("teleport_delay", 2)
                return@objTeleportTakeOff 2
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }

    companion object {
        private val SHRINE_TILE = Tile(2452, 4476)
        private val ENTRANCE = Tile(1563, 4357)
        private val ROPE_TICKS = TimeUnit.MINUTES.toTicks(3)
    }
}
