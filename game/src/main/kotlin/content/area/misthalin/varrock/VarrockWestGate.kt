package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script

class VarrockWestGate : Script {
    init {
        objectOperate("Open", "gate_west_varrock_closed,gate_west_varrock_2_closed") { (target) ->
            enterDoor(target)
        }
    }
}
