package rs.dusk.engine.entity.item

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class FloorItemOption(override val player: Player, val floorItem: FloorItem, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<FloorItemOption>
}