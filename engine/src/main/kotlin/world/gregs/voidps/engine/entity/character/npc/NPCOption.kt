package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class NPCOption(override val player: Player, val npc: NPC, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<NPCOption>
}