package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC

interface TargetNPCContext : CharacterContext {
    val target: NPC
}