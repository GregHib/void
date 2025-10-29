package content.area.misthalin.varrock

import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.entity.character.npc.hunt.huntFloorItem
import world.gregs.voidps.engine.event.Script

@Script
class AshCleaner {

    init {
        huntFloorItem("ash_cleaner", mode = "ash_finder") { npc ->
            npc.interactFloorItem(target, "Take")
        }
    }
}
