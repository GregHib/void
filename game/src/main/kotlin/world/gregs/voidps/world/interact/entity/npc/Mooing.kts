package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import kotlin.random.Random

on<Registered>({ it.def.name == "Cow" }) { npc: NPC ->
    npc.action(ActionType.Misc) {
        while(isActive) {
            delay(ticks = Random.nextInt(50, 200))
            npc.movement.clear()
            npc.forceChat = "Moo"
            npc.setAnimation("cow_eat_grass")
        }
    }
}