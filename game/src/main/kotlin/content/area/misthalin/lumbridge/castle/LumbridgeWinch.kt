package content.area.misthalin.lumbridge.castle

import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class LumbridgeWinch : Script {

    init {
        objectOperate("Operate", "lumbridge_winch") { (target) ->
            message("It seems the winch is jammed. You can't move it.")
            areaSound("lever", target.tile)
        }
    }
}
