package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.details
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.*
import world.gregs.voidps.utility.get

/**
 * API for the interacting and tracking of client interfaces
 */
class Interfaces(
    private val events: Events,
    var client: Client? = null,
    val definitions: InterfaceDefinitions,
    private val gameFrame: PlayerGameFrame,
    private val openInterfaces: MutableSet<String> = mutableSetOf()
) {

    fun open(name: String): Boolean {
        if (!hasOpenOrRootParent(name)) {
            return false
        }
        return sendIfOpened(name)
    }

    fun close(name: String): Boolean {
        if (remove(name)) {
            closeChildrenOf(name)
            return true
        }
        return false
    }

    fun closeChildren(name: String): Boolean {
        if (contains(name)) {
            closeChildrenOf(name)
            return true
        }
        return false
    }

    fun remove(name: String): Boolean {
        if (openInterfaces.remove(name)) {
            val inter = definitions.get(name).details
            sendClose(inter)
            events.emit(InterfaceClosed(inter.id, inter.name))
            return true
        }
        return false
    }

    fun get(type: String): String? {
        return openInterfaces.firstOrNull { definitions.get(it).details.type == type }
    }

    fun contains(id: Int): Boolean = contains(definitions.get(id).details.name)

    fun contains(name: String): Boolean {
        return openInterfaces.contains(name)
    }

    fun refresh() {
        openInterfaces.forEach {
            val inter = definitions.get(it).details
            sendOpen(inter)
            notifyRefreshed(inter)
        }
    }

    private fun hasOpenOrRootParent(name: String): Boolean {
        val parent = definitions.get(name).details.getParent(gameFrame.resizable)
        return parent == ROOT_ID || contains(parent)
    }

    private fun sendIfOpened(name: String): Boolean {
        if (openInterfaces.add(name)) {
            val inter = definitions.get(name).details
            sendOpen(inter)
            events.emit(InterfaceOpened(inter.id, inter.name))
            return true
        }
        val inter = definitions.get(name).details
        notifyRefreshed(inter)
        return false
    }

    private fun closeChildrenOf(parent: String) {
        getChildren(parent).forEach(::close)
    }

    private fun getChildren(parent: String): List<String> =
        openInterfaces.filter { name -> definitions.get(name).details.getParent(gameFrame.resizable) == parent }

    private fun sendOpen(inter: InterfaceDetail) {
        val parent = inter.getParent(gameFrame.resizable)
        if (parent == ROOT_ID) {
            client?.updateInterface(inter.id, 0)
        } else {
            val index = inter.getIndex(gameFrame.resizable)
            val permanent = inter.type != "main_screen" && inter.type != "underlay" && inter.type != "dialogue_box"
            client?.openInterface(permanent, definitions.get(parent).details.id, index, inter.id)
        }
    }

    private fun sendClose(inter: InterfaceDetail) {
        val index = inter.getIndex(gameFrame.resizable)
        val parent = inter.getParent(gameFrame.resizable)
        client?.closeInterface(definitions.get(parent).details.id, index)
    }

    private fun notifyRefreshed(inter: InterfaceDetail) {
        events.emit(InterfaceRefreshed(inter.id, inter.name))
    }

    companion object {
        const val ROOT_ID = "root"
        const val ROOT_INDEX = 0
    }
}

fun Interfaces.sendPlayerHead(name: String, component: String): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.playerDialogueHead(comp.parent, comp.id)
    return true
}

fun Interfaces.sendAnimation(name: String, component: String, animation: Int): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.animateInterface(comp.parent, comp.id, animation)
    return true
}

fun Interfaces.sendNPCHead(name: String, component: String, npc: Int): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.npcDialogueHead(comp.parent, comp.id, npc)
    return true
}

fun Interfaces.sendText(name: String, component: String, text: String): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.interfaceText(comp.parent, comp.id, text)
    return true
}

fun Interfaces.sendVisibility(name: String, component: String, visible: Boolean): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.interfaceVisibility(comp.parent, comp.id, !visible)
    return true
}

fun Interfaces.sendSprite(name: String, component: String, sprite: Int): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.interfaceSprite(comp.parent, comp.id, sprite)
    return true
}

fun Interfaces.sendItem(name: String, component: String, item: Int, amount: Int): Boolean {
    val comp = definitions.getOrNull(name)?.details?.getComponentOrNull(component) ?: return false
    client?.interfaceItem(comp.parent, comp.id, item, amount)
    return true
}

fun Player.open(interfaceName: String): Boolean {
    val defs: InterfaceDefinitions = get()
    val inter = defs.get(interfaceName).details
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