package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.arriveDelay

val teleports: Teleports by inject()

on<ObjectOption>({ operate && teleports.contains(def.stringId.ifEmpty { def.id.toString() }, target.tile, option) }) {
    arriveDelay()
    teleports.teleport(this)
}

teleportTakeOff {
    if (delay != null) {
        player.start("teleport_delay", 1)
    }
}