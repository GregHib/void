package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.NPCTargetContext
import world.gregs.voidps.engine.entity.character.npc.NPC

abstract class NPCTargetInteraction : PlayerInteraction(), NPCTargetContext {
    abstract override val target: NPC
}