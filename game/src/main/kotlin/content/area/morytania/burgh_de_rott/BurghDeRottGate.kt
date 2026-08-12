package content.area.morytania.burgh_de_rott

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script

class BurghDeRottGate : Script {

    init {
        objectOperate("Open", "burgh_de_rott_gate_*_closed") { (target) ->
            enterDoor(target, delay = 2)
        }
    }
}
