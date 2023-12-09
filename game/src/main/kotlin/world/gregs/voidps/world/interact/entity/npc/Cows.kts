package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick

on<Registered>({ it.def.name == "cow" }) { npc: NPC ->
    npc.softTimers.start("eat_grass")
}

on<TimerStart>({ timer == "eat_grass" }) { npc: NPC ->
    npc.mode = EmptyMode
    interval = random.nextInt(50, 200)
}

on<TimerTick>({ timer == "eat_grass" }) { npc: NPC ->
    npc.forceChat = "Moo"
    npc.setAnimation("cow_eat_grass")
}

on<ItemOnNPC>({ operate && target.def.name == "cow" }) { player: Player ->
    player.message("The cow doesn't want that.")
}