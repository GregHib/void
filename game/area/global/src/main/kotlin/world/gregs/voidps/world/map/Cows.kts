package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.random

npcSpawn("cow*") { npc ->
    npc.softTimers.start("eat_grass")
}

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