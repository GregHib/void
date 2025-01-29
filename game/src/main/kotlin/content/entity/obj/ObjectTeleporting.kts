package content.entity.obj

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject

val teleports: ObjectTeleports by inject()

for (option in teleports.options()) {
    objectOperate(option) {
        delay()
        teleports.teleport(this)
    }
}

objTeleportTakeOff {
    if (delay != null) {
        player.start("teleport_delay", 1)
    }
}