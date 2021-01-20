package world.gregs.void.engine.entity.character.npc

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class NPCOption(override val player: Player, val npc: NPC, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<NPCOption>
}