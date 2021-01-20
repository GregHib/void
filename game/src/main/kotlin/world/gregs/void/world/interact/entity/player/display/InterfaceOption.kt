package world.gregs.void.world.interact.entity.player.display

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

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
