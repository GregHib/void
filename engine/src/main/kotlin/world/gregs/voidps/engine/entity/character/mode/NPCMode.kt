package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.npc.NPC

interface NPCMode : Mode {
    fun tick(npc: NPC)
}