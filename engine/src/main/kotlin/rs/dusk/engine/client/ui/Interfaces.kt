package rs.dusk.engine.client.ui

import rs.dusk.engine.action.Suspension
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.client.ui.menu.InterfaceOptionSettings.getHash
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get

/**
 * Helper functions for integer and string identifiers
 */
abstract class Interfaces(private val details: InterfaceDetails) {

    fun open(name: String): Boolean = open(details.get(name))

    protected abstract fun open(inter: InterfaceDetail): Boolean

    fun close(name: String): Boolean = close(details.get(name))

    protected abstract fun close(inter: InterfaceDetail): Boolean

    abstract fun get(type: String): String?

    fun closeChildren(name: String): Boolean = closeChildren(details.get(name))

    protected abstract fun closeChildren(inter: InterfaceDetail): Boolean

    fun remove(name: String): Boolean = remove(details.get(name))

    protected abstract fun remove(inter: InterfaceDetail): Boolean

    fun contains(name: String): Boolean = contains(details.getSafe(name))

    fun contains(id: Int): Boolean = contains(details.get(id))

    protected abstract fun contains(inter: InterfaceDetail): Boolean

    abstract fun refresh()

    fun sendPlayerHead(name: String, component: String): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendPlayerHead(comp)
    }

    protected abstract fun sendPlayerHead(component: InterfaceComponentDetail): Boolean

    fun sendAnimation(name: String, component: String, animation: Int): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendAnimation(comp, animation)
    }

    protected abstract fun sendAnimation(component: InterfaceComponentDetail, animation: Int): Boolean

    fun sendNPCHead(name: String, component: String, npc: Int): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendNPCHead(comp, npc)
    }

    protected abstract fun sendNPCHead(component: InterfaceComponentDetail, npc: Int): Boolean

    fun sendText(name: String, component: String, text: String): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendText(comp, text)
    }

    protected abstract fun sendText(component: InterfaceComponentDetail, text: String): Boolean

    fun sendVisibility(name: String, component: String, visible: Boolean): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendVisibility(comp, visible)
    }

    protected abstract fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean): Boolean

    fun sendSprite(name: String, component: String, sprite: Int): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendSprite(comp, sprite)
    }

    protected abstract fun sendSprite(component: InterfaceComponentDetail, sprite: Int): Boolean

    fun sendItem(name: String, component: String, item: Int, amount: Int): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendItem(comp, item, amount)
    }

    protected abstract fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int): Boolean

    fun sendSettings(name: String, component: String, from: Int, to: Int, vararg settings: Int): Boolean {
        val comp = details.getComponent(name, component) ?: return false
        return sendSetting(comp, from, to, getHash(*settings))
    }

    protected abstract fun sendSetting(component: InterfaceComponentDetail, from: Int, to: Int, setting: Int): Boolean

    companion object {
        const val ROOT_ID = -1
        const val ROOT_INDEX = 0
    }
}

fun Player.open(interfaceName: String): Boolean {
    val lookup: InterfaceDetails = get()
    val inter = lookup.get(interfaceName)
    if (inter.type.isNotEmpty()) {
        val id = interfaces.get(inter.type)
        if (id != null) {
            interfaces.close(id)
        }
    }
    return interfaces.open(interfaceName)
}

fun Player.isOpen(interfaceName: String) = interfaces.contains(interfaceName)

fun Player.hasOpen(interfaceType: String) = interfaces.get(interfaceType) != null

fun Player.close(interfaceName: String) = interfaces.close(interfaceName)

fun Player.closeType(interfaceType: String): Boolean {
    val id = interfaces.get(interfaceType) ?: return false
    return interfaces.close(id)
}

fun Player.closeChildren(interfaceName: String) = interfaces.closeChildren(interfaceName)

suspend fun Player.awaitInterfaces(): Boolean {
    val id = interfaces.get("main_screen") ?: interfaces.get("dialogue_box") ?: interfaces.get("dialogue_box_small")
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}