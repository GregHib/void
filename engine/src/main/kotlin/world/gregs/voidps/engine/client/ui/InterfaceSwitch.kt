package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

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
