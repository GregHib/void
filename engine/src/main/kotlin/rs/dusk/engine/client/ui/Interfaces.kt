package rs.dusk.engine.client.ui

import rs.dusk.engine.action.Suspension
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get

abstract class Interfaces(private val interfaces: InterfacesLookup) {

    fun open(name: String): Boolean = open(interfaces.get(name))

    fun open(id: Int): Boolean = open(interfaces.get(id))

    protected abstract fun open(inter: Interface): Boolean

    fun close(name: String): Boolean = close(interfaces.get(name))

    fun close(id: Int): Boolean = close(interfaces.get(id))

    protected abstract fun close(inter: Interface): Boolean

    abstract fun get(type: String): Int?

    fun closeChildren(name: String): Boolean = closeChildren(interfaces.get(name))

    fun closeChildren(id: Int): Boolean = closeChildren(interfaces.get(id))

    protected abstract fun closeChildren(inter: Interface): Boolean

    fun remove(name: String): Boolean = remove(interfaces.get(name))

    fun remove(id: Int): Boolean = remove(interfaces.get(id))

    protected abstract fun remove(inter: Interface): Boolean

    fun contains(name: String): Boolean = contains(interfaces.getSafe(name))

    fun contains(id: Int): Boolean = contains(interfaces.get(id))

    protected abstract fun contains(inter: Interface): Boolean

    abstract fun refresh()

    fun sendPlayerHead(name: String, component: String): Boolean
            = interfaces.get(name, component) { inter, comp -> sendPlayerHead(inter, comp) }

    fun sendPlayerHead(id: Int, component: String): Boolean
            = interfaces.get(id, component) { inter, comp -> sendPlayerHead(inter, comp) }

    fun sendPlayerHead(name: String, component: Int): Boolean = sendPlayerHead(interfaces.getSafe(name), component)

    fun sendPlayerHead(id: Int, component: Int): Boolean = sendPlayerHead(interfaces.get(id), component)

    protected abstract fun sendPlayerHead(inter: Interface, component: Int): Boolean

    fun sendAnimation(name: String, component: String, animation: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendAnimation(inter, comp, animation) }

    fun sendAnimation(id: Int, component: String, animation: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendAnimation(inter, comp, animation) }

    fun sendAnimation(name: String, component: Int, animation: Int): Boolean
            = sendAnimation(interfaces.getSafe(name), component, animation)

    fun sendAnimation(id: Int, component: Int, animation: Int): Boolean
            = sendAnimation(interfaces.get(id), component, animation)

    protected abstract fun sendAnimation(inter: Interface, component: Int, animation: Int): Boolean

    fun sendNPCHead(name: String, component: String, npc: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendNPCHead(inter, comp, npc) }

    fun sendNPCHead(id: Int, component: String, npc: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendNPCHead(inter, comp, npc) }

    fun sendNPCHead(name: String, component: Int, npc: Int): Boolean
            = sendNPCHead(interfaces.getSafe(name), component, npc)

    fun sendNPCHead(id: Int, component: Int, npc: Int): Boolean
            = sendNPCHead(interfaces.get(id), component, npc)

    protected abstract fun sendNPCHead(inter: Interface, component: Int, npc: Int): Boolean

    fun sendText(name: String, component: String, text: String): Boolean
            = interfaces.get(name, component) { inter, comp -> sendText(inter, comp, text) }

    fun sendText(id: Int, component: String, text: String): Boolean
            = interfaces.get(id, component) { inter, comp -> sendText(inter, comp, text) }

    fun sendText(name: String, component: Int, text: String): Boolean
            = sendText(interfaces.getSafe(name), component, text)

    fun sendText(id: Int, component: Int, text: String): Boolean
            = sendText(interfaces.get(id), component, text)

    protected abstract fun sendText(inter: Interface, component: Int, text: String): Boolean

    fun sendVisibility(name: String, component: String, visible: Boolean): Boolean
            = interfaces.get(name, component) { inter, comp -> sendVisibility(inter, comp, visible) }

    fun sendVisibility(id: Int, component: String, visible: Boolean): Boolean
            = interfaces.get(id, component) { inter, comp -> sendVisibility(inter, comp, visible) }

    fun sendVisibility(name: String, component: Int, visible: Boolean): Boolean
            = sendVisibility(interfaces.getSafe(name), component, visible)

    fun sendVisibility(id: Int, component: Int, visible: Boolean): Boolean
            = sendVisibility(interfaces.get(id), component, visible)

    protected abstract fun sendVisibility(inter: Interface, component: Int, visible: Boolean): Boolean

    fun sendSprite(name: String, component: String, sprite: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendSprite(inter, comp, sprite) }

    fun sendSprite(id: Int, component: String, sprite: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendSprite(inter, comp, sprite) }

    fun sendSprite(name: String, component: Int, sprite: Int): Boolean
            = sendSprite(interfaces.getSafe(name), component, sprite)

    fun sendSprite(id: Int, component: Int, sprite: Int): Boolean
            = sendSprite(interfaces.get(id), component, sprite)

    protected abstract fun sendSprite(inter: Interface, component: Int, sprite: Int): Boolean

    fun sendItem(name: String, component: String, item: Int, amount: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendItem(inter, comp, item, amount) }

    fun sendItem(id: Int, component: String, item: Int, amount: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendItem(inter, comp, item, amount) }

    fun sendItem(name: String, component: Int, item: Int, amount: Int): Boolean
            = sendItem(interfaces.getSafe(name), component, item, amount)

    fun sendItem(id: Int, component: Int, item: Int, amount: Int): Boolean
            = sendItem(interfaces.get(id), component, item, amount)

    protected abstract fun sendItem(inter: Interface, component: Int, item: Int, amount: Int): Boolean

    fun sendSettings(name: String, component: String, from: Int, to: Int, setting: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendSettings(inter, comp, from, to, setting) }

    fun sendSettings(id: Int, component: String, from: Int, to: Int, setting: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendSettings(inter, comp, from, to, setting) }

    fun sendSettings(name: String, component: Int, from: Int, to: Int, setting: Int): Boolean
            = sendSettings(interfaces.getSafe(name), component, from, to, setting)

    fun sendSettings(id: Int, component: Int, from: Int, to: Int, setting: Int): Boolean
            = sendSettings(interfaces.get(id), component, from, to, setting)

    fun sendSettings(name: String, component: String, from: Int, to: Int, vararg settings: Int): Boolean
            = interfaces.get(name, component) { inter, comp -> sendSettings(inter, comp, from, to, settings(*settings)) }

    fun sendSettings(id: Int, component: String, from: Int, to: Int, vararg settings: Int): Boolean
            = interfaces.get(id, component) { inter, comp -> sendSettings(inter, comp, from, to, settings(*settings)) }

    fun sendSettings(name: String, component: Int, from: Int, to: Int, vararg settings: Int): Boolean
            = sendSettings(interfaces.getSafe(name), component, from, to, settings(*settings))

    fun sendSettings(id: Int, component: Int, from: Int, to: Int, vararg settings: Int): Boolean
            = sendSettings(interfaces.get(id), component, from, to, settings(*settings))

    protected abstract fun sendSettings(inter: Interface, component: Int, from: Int, to: Int, setting: Int): Boolean

    companion object {
        const val ROOT_ID = -1
        const val ROOT_INDEX = 0

        private fun settings(vararg options: Int): Int {
            var settings = 0
            for(slot in options) {
                settings = settings or (2 shl slot)
            }
            return settings
        }
    }
}

fun Player.open(interfaceName: String): Boolean {
    val lookup: InterfacesLookup = get()
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
    val id = interfaces.get("main_screen")
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}