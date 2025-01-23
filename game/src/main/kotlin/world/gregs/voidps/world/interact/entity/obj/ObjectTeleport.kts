package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject

val teleports: Teleports by inject()

for (option in teleports.options()) {
    objectOperate(option) {
        delay()
        teleports.teleport(this)
    }
}

teleportTakeOff {
    if (delay != null) {
        player.start("teleport_delay", 1)
    }
}