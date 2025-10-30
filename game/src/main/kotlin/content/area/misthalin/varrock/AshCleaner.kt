package content.area.misthalin.varrock

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.entity.character.npc.hunt.huntFloorItem

class AshCleaner : Script {

    init {
        huntFloorItem("ash_cleaner", mode = "ash_finder") { npc ->
            npc.interactFloorItem(target, "Take")
        }
    }
}
