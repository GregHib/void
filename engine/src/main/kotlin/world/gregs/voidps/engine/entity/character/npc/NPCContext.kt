package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext

interface NPCContext : CharacterContext {
    val npc: NPC
    override val character: Character
        get() = npc
}