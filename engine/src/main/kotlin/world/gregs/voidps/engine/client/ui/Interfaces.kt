package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.utility.get

/**
 * Helper functions for integer and string identifiers
 */
abstract class Interfaces(internal val details: InterfaceDetails) {

    abstract fun open(name: String): Boolean

    abstract fun close(name: String): Boolean

    abstract fun get(type: String): String?

    abstract fun closeChildren(name: String): Boolean

    abstract fun remove(name: String): Boolean

    fun contains(id: Int): Boolean = contains(details.get(id).name)

    abstract fun contains(name: String): Boolean

    abstract fun refresh()

    fun sendPlayerHead(name: String, component: String): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendPlayerHead(comp)
    }

    protected abstract fun sendPlayerHead(component: InterfaceComponentDetail): Boolean

    fun sendAnimation(name: String, component: String, animation: Int): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendAnimation(comp, animation)
    }

    protected abstract fun sendAnimation(component: InterfaceComponentDetail, animation: Int): Boolean

    fun sendNPCHead(name: String, component: String, npc: Int): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendNPCHead(comp, npc)
    }

    protected abstract fun sendNPCHead(component: InterfaceComponentDetail, npc: Int): Boolean

    fun sendText(name: String, component: String, text: String): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendText(comp, text)
    }

    protected abstract fun sendText(component: InterfaceComponentDetail, text: String): Boolean

    fun sendVisibility(name: String, component: String, visible: Boolean): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendVisibility(comp, visible)
    }

    protected abstract fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean): Boolean

    fun sendSprite(name: String, component: String, sprite: Int): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendSprite(comp, sprite)
    }

    protected abstract fun sendSprite(component: InterfaceComponentDetail, sprite: Int): Boolean

    fun sendItem(name: String, component: String, item: Int, amount: Int): Boolean {
        val comp = details.getComponentOrNull(name, component) ?: return false
        return sendItem(comp, item, amount)
    }

    protected abstract fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int): Boolean

    companion object {
        const val ROOT_ID = "root"
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

fun Player.hasScreenOpen() = hasOpen("main_screen") || hasOpen("underlay")

fun Player.close(interfaceName: String) = interfaces.close(interfaceName)

fun Player.closeType(interfaceType: String): Boolean {
    val id = interfaces.get(interfaceType) ?: return false
    return interfaces.close(id)
}

fun Player.closeChildren(interfaceName: String) = interfaces.closeChildren(interfaceName)

suspend fun Action.awaitInterface(name: String) = await<Unit>(Suspension.Interface(name))

suspend fun Player.awaitInterfaces(): Boolean {
    val id = interfaces.get("main_screen") ?: interfaces.get("underlay") ?: interfaces.get("dialogue_box") ?: interfaces.get("dialogue_box_small")
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}