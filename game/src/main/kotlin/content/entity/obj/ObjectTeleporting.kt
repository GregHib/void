package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Teleport

class ObjectTeleporting(val teleports: ObjectTeleports) : Script {

    init {
        for (option in teleports.options()) {
            objectOperate(option) { (target, option) ->
                val def = target.def(this)
                val id = def.stringId.ifEmpty { def.id.toString() }
                if (!teleports.contains(id, target.tile, option)) {
                    return@objectOperate
                }
                delay()
                teleports.teleport(this, target, option, def)
            }
        }

        objTeleportTakeOff { _, _ ->
            if (suspension != null) {
                start("teleport_delay", 1)
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }
}
