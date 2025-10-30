package content.area.misthalin.lumbridge.castle

import content.entity.sound.areaSound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate

class LumbridgeWinch : Script {

    init {
        objectOperate("Operate", "lumbridge_winch") {
            player.message("It seems the winch is jammed. You can't move it.")
            areaSound("lever", target.tile)
        }
    }
}
