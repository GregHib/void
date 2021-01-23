package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class FloorItemOption(override val player: Player, val floorItem: FloorItem, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<FloorItemOption>
}