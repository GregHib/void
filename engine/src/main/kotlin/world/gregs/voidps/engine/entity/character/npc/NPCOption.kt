package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.TargetNPCContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class NPCOption(
    override val character: Character,
    override val target: NPC,
    val def: NPCDefinition,
    val option: String
) : Interaction(), TargetNPCContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}