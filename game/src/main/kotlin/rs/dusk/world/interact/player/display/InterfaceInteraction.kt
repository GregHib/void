package rs.dusk.world.interact.player.display

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

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
