package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.random

npcSpawn("cow*") { npc: NPC ->
    npc.softTimers.start("eat_grass")
}

npcTimerStart("eat_grass") { npc: NPC ->
    npc.mode = EmptyMode
    interval = random.nextInt(50, 200)
}

npcTimerTick({ timer == "eat_grass" }) { npc: NPC ->
    npc.forceChat = "Moo"
    npc.setAnimation("cow_eat_grass")
}

itemOnNPCOperate("*", "cow*") {
    player.message("The cow doesn't want that.")
}