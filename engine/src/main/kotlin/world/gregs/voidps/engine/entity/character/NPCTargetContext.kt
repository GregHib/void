package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC

interface NPCTargetContext : CharacterContext {
    val target: NPC
}