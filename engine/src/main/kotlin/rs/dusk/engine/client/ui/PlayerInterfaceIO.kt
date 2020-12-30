package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.EventBus
import rs.dusk.network.rs.codec.game.encode.*

/**
 * Instructions to external systems
 */
class PlayerInterfaceIO(
    val player: Player,
    val bus: EventBus,
    private val openEncoder: InterfaceOpenEncoder,
    private val updateEncoder: InterfaceUpdateEncoder,
    private val animationEncoder: InterfaceAnimationEncoder,
    private val closeEncoder: InterfaceCloseEncoder,
    private val playerHeadEncoder: InterfaceHeadPlayerEncoder,
    private val npcHeadEncoder: InterfaceHeadNPCEncoder,
    private val textEncoder: InterfaceTextEncoder,
    private val visibleEncoder: InterfaceVisibilityEncoder,
    private val spriteEncoder: InterfaceSpriteEncoder,
    private val itemEncoder: InterfaceItemEncoder
) : InterfaceIO {

    override fun sendOpen(inter: InterfaceDetail) {
        val parent = inter.getParent(player.gameFrame.resizable)
        if (parent == -1) {
            updateEncoder.encode(player, inter.id, 0)
        } else {
            val index = inter.getIndex(player.gameFrame.resizable)
            val permanent = inter.type != "main_screen" && inter.type != "dialogue_box"
            openEncoder.encode(player, permanent, parent, index, inter.id)
        }
    }

    override fun sendClose(inter: InterfaceDetail) {
        val index = inter.getIndex(player.gameFrame.resizable)
        val parent = inter.getParent(player.gameFrame.resizable)
        closeEncoder.encode(player, parent, index)
    }

    override fun notifyClosed(inter: InterfaceDetail) {
        bus.emit(InterfaceClosed(player, inter.id, inter.name))
    }

    override fun notifyOpened(inter: InterfaceDetail) {
        bus.emit(InterfaceOpened(player, inter.id, inter.name))
    }

    override fun notifyRefreshed(inter: InterfaceDetail) {
        bus.emit(InterfaceRefreshed(player, inter.id, inter.name))
    }

    override fun sendPlayerHead(component: InterfaceComponentDetail) {
        playerHeadEncoder.encode(player, component.parent, component.id)
    }

    override fun sendAnimation(component: InterfaceComponentDetail, animation: Int) {
        animationEncoder.encode(player, component.parent, component.id, animation)
    }

    override fun sendNPCHead(component: InterfaceComponentDetail, npc: Int) {
        npcHeadEncoder.encode(player, component.parent, component.id, npc)
    }

    override fun sendText(component: InterfaceComponentDetail, text: String) {
        textEncoder.encode(player, component.parent, component.id, text)
    }

    override fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean) {
        visibleEncoder.encode(player, component.parent, component.id, !visible)
    }

    override fun sendSprite(component: InterfaceComponentDetail, sprite: Int) {
        spriteEncoder.encode(player, component.parent, component.id, sprite)
    }

    override fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int) {
        itemEncoder.encode(player, component.parent, component.id, item, amount)
    }
}