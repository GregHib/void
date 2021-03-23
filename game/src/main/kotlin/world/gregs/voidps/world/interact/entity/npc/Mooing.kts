package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCRegistered
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import kotlin.random.Random

val eatGrassAnimation = 5854
on<NPCRegistered>({ npc.def.name == "Cow" }) { npc: NPC ->
    action(ActionType.Misc) {
        while(isActive) {
            delay(ticks = Random.nextInt(50, 200))
            npc.movement.clear()
            npc.forceChat = "Moo"
            npc.setAnimation(eatGrassAnimation)
        }
    }
}