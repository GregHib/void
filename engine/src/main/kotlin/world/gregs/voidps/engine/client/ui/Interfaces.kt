package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.getComponentOrNull
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.*

/**
 * API for the interacting and tracking of client interfaces
 */
class Interfaces(
    private val events: Events,
    var client: Client? = null,
    val definitions: InterfaceDefinitions,
    private val gameFrame: GameFrame,
    private val openInterfaces: MutableSet<String> = ObjectOpenHashSet()
) {

    fun open(id: String): Boolean {
        if (!hasOpenOrRootParent(id)) {
            return false
        }
        return sendIfOpened(id)
    }

    fun close(id: String?): Boolean {
        if (id != null && !getType(id).startsWith("dialogue_box")) {
            events.emit(CloseInterface)
        }
        if (id != null && remove(id)) {
            closeChildrenOf(id)
            return true
        }
        return false
    }

    fun closeChildren(id: String): Boolean {
        if (contains(id)) {
            closeChildrenOf(id)
            return true
        }
        return false
    }

    fun remove(id: String): Boolean {
        if (openInterfaces.remove(id)) {
            sendClose(id)
            events.emit(InterfaceClosed(id))
            return true
        }
        return false
    }

    fun get(type: String): String? {
        return openInterfaces.firstOrNull { getType(it) == type }
    }

    fun contains(id: String): Boolean {
        return openInterfaces.contains(id)
    }

    fun refresh() {
        openInterfaces.forEach { id ->
            sendOpen(id)
            notifyRefresh(id)
        }
    }

    private fun hasOpenOrRootParent(id: String): Boolean {
        val parent = getParent(id)
        return parent == ROOT_ID || contains(parent)
    }

    private fun sendIfOpened(id: String): Boolean {
        if (openInterfaces.add(id)) {
            sendOpen(id)
            events.emit(InterfaceOpened(id))
            notifyRefresh(id)
            return true
        }
        notifyRefresh(id)
        return false
    }

    private fun closeChildrenOf(parent: String) {
        val it = openInterfaces.iterator()
        val children = mutableListOf<String>()
        while (it.hasNext()) {
            val id = it.next()
            if (getParent(id) == parent) {
                it.remove()
                sendClose(id)
                events.emit(InterfaceClosed(id))
                children.add(id)
            }
        }
        for (child in children) {
            closeChildrenOf(child)
        }
    }

    private fun getParent(id: String): String {
        return definitions.get(id)[if (gameFrame.resizable) "parent_resize" else "parent_fixed", ""]
    }

    private fun getIndex(id: String): Int {
        return definitions.get(id)[if (gameFrame.resizable) "index_resize" else "index_fixed", -1]
    }

    private fun getType(id: String): String {
        return definitions.get(id)["type", "main_screen"]
    }

    private fun sendOpen(id: String) {
        val parent = getParent(id)
        if (parent == ROOT_ID) {
            client?.updateInterface(definitions.get(id).id, 0)
        } else {
            val type = getType(id)
            val permanent = type != "main_screen" && type != "underlay" && type != "dialogue_box"
            client?.openInterface(
                permanent = permanent,
                parent = definitions.get(parent).id,
                component = getIndex(id),
                id = definitions.get(id).id
            )
        }
    }

    private fun sendClose(id: String) {
        val parent = getParent(id)
        client?.closeInterface(definitions.get(parent).id, getIndex(id))
    }

    private fun notifyRefresh(id: String) {
        events.emit(InterfaceRefreshed(id))
    }

    companion object {
        const val ROOT_ID = "root"
        const val ROOT_INDEX = 0
    }
}

private fun getComponent(id: String, component: String): InterfaceComponentDefinition? {
    val definitions: InterfaceDefinitions = get()
    return definitions.get(id).getComponentOrNull(component)
}

fun Interfaces.sendAnimation(id: String, component: String, animation: Int): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.animateInterface(comp["parent", -1], comp.id, animation)
    return true
}

fun Interfaces.sendText(id: String, component: String, text: String): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.interfaceText(comp["parent", -1], comp.id, Colours.replaceCustomTags(text))
    return true
}

fun Interfaces.sendVisibility(id: String, component: String, visible: Boolean): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.interfaceVisibility(comp["parent", -1], comp.id, !visible)
    return true
}

fun Interfaces.sendSprite(id: String, component: String, sprite: Int): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.interfaceSprite(comp["parent", -1], comp.id, sprite)
    return true
}

fun Interfaces.sendItem(id: String, component: String, item: Int, amount: Int): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.interfaceItem(comp["parent", -1], comp.id, item, amount)
    return true
}

fun Interfaces.sendItem(id: String, component: String, item: Item): Boolean {
    val comp = getComponent(id, component) ?: return false
    client?.interfaceItem(comp["parent", -1], comp.id, item.def.id, item.amount)
    return true
}

/**
 * @param close any interfaces open with the same type
 */
fun Player.open(interfaceId: String, close: Boolean = true): Boolean {
    val defs: InterfaceDefinitions = get()
    val type = defs.get(interfaceId)["type", ""]
    if (close && type.isNotEmpty()) {
        interfaces.get(type)?.let {
            interfaces.close(it)
        }
    }
    return interfaces.open(interfaceId)
}

fun Player.hasOpen(interfaceId: String) = interfaces.contains(interfaceId)

fun Player.hasTypeOpen(interfaceType: String) = interfaces.get(interfaceType) != null

fun Character.hasScreenOpen(): Boolean {
    if (this !is Player) {
        return false
    }
    return hasTypeOpen("main_screen") || hasTypeOpen("underlay")
}

fun Player.close(interfaceId: String?) = interfaces.close(interfaceId)

fun Player.closeType(interfaceType: String): Boolean {
    val id = interfaces.get(interfaceType) ?: return false
    return interfaces.close(id)
}

fun Player.closeChildren(interfaceId: String) = interfaces.closeChildren(interfaceId)

val Player.dialogue: String?
    get() = interfaces.get("dialogue_box") ?: interfaces.get("dialogue_box_small")

val Player.menu: String?
    get() = interfaces.get("main_screen") ?: interfaces.get("underlay") ?: dialogue

fun Player.closeDialogue(): Boolean {
    if (dialogueSuspension != null) {
        dialogueSuspension = null
    }
    return closeType("dialogue_box") || closeType("dialogue_box_small")
}

fun Player.closeMenu(): Boolean = close(menu)

fun Player.closeInterfaces(): Boolean {
    var closed = closeDialogue()
    if (closeMenu()) {
        closed = true
    }
    queue.clearWeak()
    return closed
}