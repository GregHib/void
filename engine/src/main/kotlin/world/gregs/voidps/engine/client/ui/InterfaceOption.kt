package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class InterfaceOption(
    override val player: Player,
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val optionId: Int,
    val option: String,
    val item: String,
    val itemId: Int,
    val itemIndex: Int
) : PlayerEvent() {
    companion object : EventCompanion<InterfaceOption>
}
