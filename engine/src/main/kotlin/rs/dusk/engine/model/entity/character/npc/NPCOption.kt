package rs.dusk.engine.model.entity.character.npc

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class NPCOption(override val player: Player, val npc: NPC, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<NPCOption>
}