package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.arriveDelay

val teleports: Teleports by inject()

objectOperate("*") {
    if (teleports.contains(def.stringId.ifEmpty { def.id.toString() }, target.tile, option)) {
        arriveDelay()
        teleports.teleport(this)
    }
}

teleport({ takeoff && delay != null }, Priority.LOWEST) { player: Player ->
    player.start("teleport_delay", 1)
}