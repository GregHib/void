package content.area.kandarin.barbarian_outpost

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import content.quest.questCompleted
import content.entity.obj.door.enterDoor

val npcs: NPCs by inject()

objectOperate("Open", "barbarian_outpost_gate_left_closed", "barbarian_outpost_gate_right_closed") {
    if (!player.questCompleted("alfred_grimhands_barcrawl")) {
        val guard = npcs[player.tile.regionLevel].firstOrNull { it.id == "barbarian_guard" } ?: return@objectOperate
        player.talkWith(guard)
        player.mode = Interact(player, guard, NPCOption(player, guard, guard.def, "Talk-to"))
        return@objectOperate
    }
    player.walkToDelay(player.tile.copy(y = player.tile.y.coerceIn(2569, 3570)))
    enterDoor(target, delay = 2)
}