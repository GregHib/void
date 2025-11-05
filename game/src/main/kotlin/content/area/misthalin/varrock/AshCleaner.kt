package content.area.misthalin.varrock

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem

class AshCleaner : Script {

    init {
        huntFloorItem("ash_finder") { target ->
            interactFloorItem(target, "Take")
        }
    }
}
