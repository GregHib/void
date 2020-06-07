package rs.dusk.engine.model.entity.obj

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

data class ObjectOption(override val player: Player, val location: Location, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<ObjectOption>
}