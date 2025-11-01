package content.area.kandarin.barbarian_outpost

import content.entity.obj.door.enterDoor
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inject

class BarbarianOutpostGate : Script {

    val npcs: NPCs by inject()

    init {
        objectOperate("Open", "barbarian_outpost_gate_left_closed,barbarian_outpost_gate_right_closed") { (target) ->
            if (!questCompleted("alfred_grimhands_barcrawl")) {
                val guard = npcs[tile.regionLevel].firstOrNull { it.id == "barbarian_guard" } ?: return@objectOperate
                talkWith(guard)
                interactNpc(guard, "Talk-to")
                return@objectOperate
            }
            walkToDelay(tile.copy(y = tile.y.coerceIn(2569, 3570)))
            enterDoor(target, delay = 2)
        }
    }
}
