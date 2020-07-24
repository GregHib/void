package rs.dusk.engine.model.entity.item

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class FloorItemOption(override val player: Player, val floorItem: FloorItem, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<FloorItemOption>
}