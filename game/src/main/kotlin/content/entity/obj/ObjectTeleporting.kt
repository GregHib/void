package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Teleport

class ObjectTeleporting(val teleports: ObjectTeleports) : Script {

    init {
        for (option in teleports.options()) {
            objectOperate(option) { (target, option) ->
                delay()
                teleports.teleport(this, target, option)
            }
        }

        objTeleportTakeOff { _, _ ->
            if (delay != null) {
                start("teleport_delay", 1)
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }
}
