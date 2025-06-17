package content.area.misthalin.varrock

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.hunt.huntFloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption

huntFloorItem("ash_cleaner", mode = "ash_finder") { npc ->
    npc.mode = Interact(npc, target, FloorItemOption(npc, target, "Take"))
}
