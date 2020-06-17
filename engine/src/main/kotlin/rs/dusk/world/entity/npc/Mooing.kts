package rs.dusk.world.entity.npc

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.index.npc.NPCRegistered
import rs.dusk.engine.model.entity.index.update.visual.forceChat
import rs.dusk.engine.model.entity.index.update.visual.setAnimation
import kotlin.random.Random

val eatGrassAnimation = 5854

NPCRegistered where { npc.def.name == "Cow" } then {
    action(ActionType.Misc) {
        while(active) {
            delay(ticks = Random.nextInt(50, 200))
            npc.movement.clear()
            npc.forceChat = "Moo"
            npc.setAnimation(eatGrassAnimation)
        }
    }
}