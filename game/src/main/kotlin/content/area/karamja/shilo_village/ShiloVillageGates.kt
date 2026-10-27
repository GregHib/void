package content.area.karamja.shilo_village

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script

class ShiloVillageGates : Script {
    init {
        objectOperate("Open", "metal_gate_closed,metal_gate_2_closed,wooden_gate_closed,wooden_gate_2_closed") { (target) ->
            enterDoor(target)
        }
    }
}
