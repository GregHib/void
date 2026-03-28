package content.area.wilderness.chaos_tunnels

import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.queue.queue

class ChaosTunnels(val teleports: ObjectTeleports) : Script {
    init {
        objTeleportLand("Enter", "chaos_tunnels_portal") { _, _ ->
            gfx("curse_impact")
        }

        objTeleportTakeOff("Enter", "chaos_tunnels_rift_west") { target, _ ->
            rift(target, "west")
        }

        objTeleportTakeOff("Enter", "chaos_tunnels_rift") { target, _ ->
            rift(target, "central")
        }

        objTeleportTakeOff("Enter", "chaos_tunnels_rift_east") { target, _ ->
            rift(target, "east")
        }
    }

    private fun Player.rift(target: GameObject, direction: String): Int = if (get("warning_chaos_tunnels_$direction", 0) == 7) {
        Teleport.CONTINUE
    } else {
        queue("warning_chaos_tunnels_$direction") {
            if (warning("chaos_tunnels_$direction")) {
                val definition = teleports.get("Enter")[target.tile.id]!!
                teleports.teleportContinue(this@rift, definition, target)
            }
        }
        Teleport.CANCEL
    }
}
