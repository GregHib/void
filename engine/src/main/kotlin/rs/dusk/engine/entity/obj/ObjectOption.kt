package rs.dusk.engine.entity.obj

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class ObjectOption(override val player: Player, val obj: GameObject, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<ObjectOption>
}