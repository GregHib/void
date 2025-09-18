package content.area.misthalin.lumbridge.farm

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.random

@Script
class Cows : Api {

    override fun spawn(npc: NPC) {
        if (npc.id.startsWith("cow")) {
            npc.softTimers.start("eat_grass")
        }
    }

    init {
        npcTimerStart("eat_grass") { npc ->
            npc.mode = EmptyMode
            interval = random.nextInt(50, 200)
        }

        npcTimerTick("eat_grass") { npc ->
            if (npc.mode == EmptyMode) {
                npc.say("Moo")
                npc.anim("cow_eat_grass")
            }
        }

        itemOnNPCOperate("*", "cow*") {
            player.message("The cow doesn't want that.")
        }
    }
}
