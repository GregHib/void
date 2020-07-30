package rs.dusk.engine.entity.character.npc

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class NPCOption(override val player: Player, val npc: NPC, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<NPCOption>
}