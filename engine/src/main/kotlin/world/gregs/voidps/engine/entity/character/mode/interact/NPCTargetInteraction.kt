package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.npc.NPC

abstract class NPCTargetInteraction : PlayerInteraction() {
    abstract val npc: NPC
}