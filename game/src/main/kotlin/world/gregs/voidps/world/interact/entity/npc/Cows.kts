package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.delay
import kotlin.random.Random

on<Registered>({ it.def.name == "cow" }) { npc: NPC ->
    npc.delay(ticks = Random.nextInt(50, 200), loop = true) {
        npc.movement.clear()
        npc.forceChat = "Moo"
        npc.setAnimation("cow_eat_grass")
    }
}

on<InterfaceOnNPC>({ npc.def.name == "cow" }) { player: Player ->
    player.message("The cow doesn't want that.")
}