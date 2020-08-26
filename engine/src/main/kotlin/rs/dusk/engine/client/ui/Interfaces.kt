package rs.dusk.engine.client.ui

import rs.dusk.engine.action.Suspension
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

    fun sendPlayerHead(name: String, component: String): Boolean
            = details.get(name, component) { inter, comp -> sendPlayerHead(inter, comp) }

    protected abstract fun sendPlayerHead(inter: InterfaceDetail, component: Int): Boolean

    fun sendAnimation(name: String, component: String, animation: Int): Boolean
            = details.get(name, component) { inter, comp -> sendAnimation(inter, comp, animation) }

    protected abstract fun sendAnimation(inter: InterfaceDetail, component: Int, animation: Int): Boolean

    fun sendNPCHead(name: String, component: String, npc: Int): Boolean
            = details.get(name, component) { inter, comp -> sendNPCHead(inter, comp, npc) }

    protected abstract fun sendNPCHead(inter: InterfaceDetail, component: Int, npc: Int): Boolean

    fun sendText(name: String, component: String, text: String): Boolean
            = details.get(name, component) { inter, comp -> sendText(inter, comp, text) }

    protected abstract fun sendText(inter: InterfaceDetail, component: Int, text: String): Boolean

    fun sendVisibility(name: String, component: String, visible: Boolean): Boolean
            = details.get(name, component) { inter, comp -> sendVisibility(inter, comp, visible) }

    protected abstract fun sendVisibility(inter: InterfaceDetail, component: Int, visible: Boolean): Boolean

    fun sendSprite(name: String, component: String, sprite: Int): Boolean
            = details.get(name, component) { inter, comp -> sendSprite(inter, comp, sprite) }

    protected abstract fun sendSprite(inter: InterfaceDetail, component: Int, sprite: Int): Boolean

    fun sendItem(name: String, component: String, item: Int, amount: Int): Boolean
            = details.get(name, component) { inter, comp -> sendItem(inter, comp, item, amount) }

    protected abstract fun sendItem(inter: InterfaceDetail, component: Int, item: Int, amount: Int): Boolean

    fun sendSetting(name: String, component: String, from: Int, to: Int, setting: Int): Boolean
            = details.get(name, component) { inter, comp -> sendSetting(inter, comp, from, to, setting) }

    fun sendSettings(name: String, component: String, from: Int, to: Int, vararg settings: Int): Boolean
            = details.get(name, component) { inter, comp -> sendSetting(inter, comp, from, to, getHash(*settings)) }

    protected abstract fun sendSetting(inter: InterfaceDetail, component: Int, from: Int, to: Int, setting: Int): Boolean

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