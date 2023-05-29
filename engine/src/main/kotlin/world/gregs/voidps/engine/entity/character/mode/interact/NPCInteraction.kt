package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.npc.NPC

abstract class NPCInteraction : Interaction() {
    abstract val npc: NPC
}