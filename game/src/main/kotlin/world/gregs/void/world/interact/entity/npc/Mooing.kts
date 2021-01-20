package world.gregs.void.world.interact.entity.npc

import world.gregs.void.engine.action.ActionType
import world.gregs.void.engine.action.action
import world.gregs.void.engine.entity.character.npc.NPCRegistered
import world.gregs.void.engine.entity.character.update.visual.forceChat
import world.gregs.void.engine.entity.character.update.visual.setAnimation
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import kotlin.random.Random

val eatGrassAnimation = 5854

NPCRegistered where { npc.def.name == "Cow" } then {
    action(ActionType.Misc) {
        while(isActive) {
            delay(ticks = Random.nextInt(50, 200))
            npc.movement.clear()
            npc.forceChat = "Moo"
            npc.setAnimation(eatGrassAnimation)
        }
    }
}