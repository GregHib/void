package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntFloorItem
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.delay

on<HuntFloorItem>({ it.id == "ash_cleaner" && mode == "ash_finder" }) { npc: NPC ->
    npc.mode = Interact(npc, target, FloorItemOption(npc, target, "Take"))
}

on<FloorItemOption>({ operate && option == "Take" && it.id == "ash_cleaner" }, Priority.LOW) { npc: NPC ->
    npc.setAnimation("cleaner_sweeping")
    delay(2)
    npc.clearAnimation()
}