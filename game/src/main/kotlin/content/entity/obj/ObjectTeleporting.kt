package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.inject

class ObjectTeleporting : Script {

    val teleports: ObjectTeleports by inject()

    init {
        for (option in teleports.options()) {
            objectOperate(option) { (target, option) ->
                delay()
                teleports.teleport(this, target, option)
            }
        }

        objTeleportTakeOff {
            if (delay != null) {
                player.start("teleport_delay", 1)
            }
        }
    }
}
