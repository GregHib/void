package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.npc.NPCRegistered
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
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