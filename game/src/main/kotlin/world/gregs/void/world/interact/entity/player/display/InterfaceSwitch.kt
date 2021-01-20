package world.gregs.void.world.interact.entity.player.display

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class InterfaceSwitch(
    override val player: Player,
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val fromItemId: Int,
    val fromSlot: Int,
    val toId: Int,
    val toName: String,
    val toComponentId: Int,
    val toComponent: String,
    val toItemId: Int,
    val toSlot: Int
) : PlayerEvent() {
    companion object : EventCompanion<InterfaceSwitch>
}
