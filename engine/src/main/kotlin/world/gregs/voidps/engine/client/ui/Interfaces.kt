package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.*
import kotlin.coroutines.resume

/**
 * API for the interacting and tracking of client interfaces
 */
class Interfaces(
    private val events: Events,
    var client: Client? = null,
    val definitions: InterfaceDefinitions,
    private val gameFrame: PlayerGameFrame,
    private val openInterfaces: MutableSet<String> = ObjectOpenHashSet()
) {

    fun open(id: String): Boolean {
        if (!hasOpenOrRootParent(id)) {
            return false
        }
        return sendIfOpened(id)
    }

    fun close(id: String): Boolean {
        if (remove(id)) {
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
    client?.interfaceText(comp["parent", -1], comp.id, text)
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

fun Player.open(interfaceId: String): Boolean {
    val defs: InterfaceDefinitions = get()
    val type = defs.get(interfaceId)["type", ""]
    if (type.isNotEmpty()) {
        interfaces.get(type)?.let {
            interfaces.close(it)
        }
    }
    return interfaces.open(interfaceId)
}

fun Player.isOpen(interfaceId: String) = interfaces.contains(interfaceId)

fun Player.hasOpen(interfaceType: String) = interfaces.get(interfaceType) != null

fun Player.hasScreenOpen() = hasOpen("main_screen") || hasOpen("underlay")

fun Player.close(interfaceId: String) = interfaces.close(interfaceId)

fun Player.closeType(interfaceType: String): Boolean {
    val id = interfaces.get(interfaceType) ?: return false
    return interfaces.close(id)
}

fun Player.closeChildren(interfaceId: String) = interfaces.closeChildren(interfaceId)

suspend fun Action.awaitInterface(id: String) = await<Unit>(Suspension.Interface(id))

suspend fun <T : Any> Action.await(job: Deferred<T>): T = suspendCancellableCoroutine { cont ->
    continuation = cont
    this.suspension = Suspension.External
    job.invokeOnCompletion {
        if (it == null) {
            cont.resume(job.getCompleted())
        }
    }
}

val Player.dialogue: String?
    get() = interfaces.get("dialogue_box") ?: interfaces.get("dialogue_box_small")

val Player.menu: String?
    get() = interfaces.get("main_screen") ?: interfaces.get("underlay") ?: dialogue

fun Player.closeDialogue(): Boolean {
    return close(dialogue ?: return false)
}

fun Player.closeInterface(): Boolean {
    return close(menu ?: return false)
}

suspend fun Player.awaitDialogues(): Boolean {
    val id = dialogue
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}

suspend fun Player.awaitInterfaces(): Boolean {
    val id = menu
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}