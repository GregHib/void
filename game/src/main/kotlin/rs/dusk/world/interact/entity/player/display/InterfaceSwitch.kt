package rs.dusk.world.interact.entity.player.display

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

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
