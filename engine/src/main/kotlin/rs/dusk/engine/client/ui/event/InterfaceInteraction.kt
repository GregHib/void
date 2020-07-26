package rs.dusk.engine.client.ui.event

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class InterfaceInteraction(
    override val player: Player,
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val optionId: Int,
    val option: String,
    val paramOne: Int,
    val paramTwo: Int
) : PlayerEvent() {
    companion object : EventCompanion<InterfaceInteraction>
}
