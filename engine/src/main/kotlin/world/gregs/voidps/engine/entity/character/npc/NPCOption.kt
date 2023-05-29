package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.mode.interact.NPCInteraction
import world.gregs.voidps.engine.entity.character.player.Player

data class NPCOption(
    override val player: Player,
    override val npc: NPC,
    val def: NPCDefinition,
    val option: String
) : NPCInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}