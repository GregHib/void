package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.*

/**
 * Instructions to external systems
 */
class PlayerInterfaceIO(
    val player: Player
) : InterfaceIO {

    override fun sendPlayerHead(component: InterfaceComponentDetail) {
        player.client?.playerDialogueHead(component.parent, component.id)
    }

    override fun sendAnimation(component: InterfaceComponentDetail, animation: Int) {
        player.client?.animateInterface(component.parent, component.id, animation)
    }

    override fun sendNPCHead(component: InterfaceComponentDetail, npc: Int) {
        player.client?.npcDialogueHead(component.parent, component.id, npc)
    }

    override fun sendText(component: InterfaceComponentDetail, text: String) {
        player.client?.interfaceText(component.parent, component.id, text)
    }

    override fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean) {
        player.client?.interfaceVisibility(component.parent, component.id, !visible)
    }

    override fun sendSprite(component: InterfaceComponentDetail, sprite: Int) {
        player.client?.interfaceSprite(component.parent, component.id, sprite)
    }

    override fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int) {
        player.client?.interfaceItem(component.parent, component.id, item, amount)
    }
}