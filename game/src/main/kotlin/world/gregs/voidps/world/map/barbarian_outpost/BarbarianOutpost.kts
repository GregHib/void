package world.gregs.voidps.world.map.barbarian_outpost

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.entity.obj.door.Door

val npcs: NPCs by inject()

objectOperate("Open", "barbarian_outpost_gate_left_closed", "barbarian_outpost_gate_right_closed") {
    if (player.questComplete("alfred_grimhands_barcrawl")) {
        Door.enter(player, target)
    } else {
        val guard = npcs[player.tile.regionLevel].firstOrNull { it.id == "barbarian_guard" } ?: return@objectOperate
        player.mode = Interact(player, guard, NPCOption(player, guard, guard.def, "Talk-to"))
    }
}