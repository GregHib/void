package world.gregs.voidps.engine.client.ui

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
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.*
import world.gregs.voidps.utility.get
import kotlin.coroutines.resume

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
            sendClose(name)
            events.emit(InterfaceClosed(definitions.getId(name), name))
            return true
        }
        return false
    }

    fun get(type: String): String? {
        return openInterfaces.firstOrNull { getType(it) == type }
    }

    fun contains(id: Int): Boolean = contains(definitions.getName(id))

    fun contains(name: String): Boolean {
        return openInterfaces.contains(name)
    }

    fun refresh() {
        openInterfaces.forEach { name ->
            sendOpen(name)
            notifyRefresh(name)
        }
    }

    private fun hasOpenOrRootParent(name: String): Boolean {
        val parent = getParent(name)
        return parent == ROOT_ID || contains(parent)
    }

    private fun sendIfOpened(name: String): Boolean {
        if (openInterfaces.add(name)) {
            sendOpen(name)
            events.emit(InterfaceOpened(definitions.getId(name), name))
            notifyRefresh(name)
            return true
        }
        notifyRefresh(name)
        return false
    }

    private fun closeChildrenOf(parent: String) {
        getChildren(parent).forEach(::close)
    }

    private fun getChildren(parent: String): List<String> =
        openInterfaces.filter { name -> getParent(name) == parent }

    private fun getParent(name: String): String {
        return definitions.get(name)[if (gameFrame.resizable) "parent_resize" else "parent_fixed", ""]
    }

    private fun getIndex(name: String): Int {
        return definitions.get(name)[if (gameFrame.resizable) "index_resize" else "index_fixed", -1]
    }

    private fun getType(name: String): String {
        return definitions.get(name)["type", "main_screen"]
    }

    private fun sendOpen(name: String) {
        val parent = getParent(name)
        if (parent == ROOT_ID) {
            client?.updateInterface(definitions.getId(name), 0)
        } else {
            val type = getType(name)
            val permanent = type != "main_screen" && type != "underlay" && type != "dialogue_box"
            client?.openInterface(
                permanent = permanent,
                parent = definitions.getId(parent),
                component = getIndex(name),
                id = definitions.getId(name)
            )
        }
    }

    private fun sendClose(name: String) {
        val parent = getParent(name)
        client?.closeInterface(definitions.getId(parent), getIndex(name))
    }

    private fun notifyRefresh(name: String) {
        events.emit(InterfaceRefreshed(definitions.getId(name), name))
    }

    companion object {
        const val ROOT_ID = "root"
        const val ROOT_INDEX = 0
    }
}

private fun getComponent(name: String, componentName: String): InterfaceComponentDefinition? {
    val definitions: InterfaceDefinitions = get()
    return definitions.get(name).getComponentOrNull(componentName)
}

fun Interfaces.sendAnimation(name: String, component: String, animation: Int): Boolean {
    val comp = getComponent(name, component) ?: return false
    client?.animateInterface(comp["parent", -1], comp.id, animation)
    return true
}

fun Interfaces.sendText(name: String, component: String, text: String): Boolean {
    val comp = getComponent(name, component) ?: return false
    client?.interfaceText(comp["parent", -1], comp.id, text)
    return true
}

fun Interfaces.sendVisibility(name: String, component: String, visible: Boolean): Boolean {
    val comp = getComponent(name, component) ?: return false
    client?.interfaceVisibility(comp["parent", -1], comp.id, !visible)
    return true
}

fun Interfaces.sendSprite(name: String, component: String, sprite: Int): Boolean {
    val comp = getComponent(name, component) ?: return false
    client?.interfaceSprite(comp["parent", -1], comp.id, sprite)
    return true
}

fun Interfaces.sendItem(name: String, component: String, item: Int, amount: Int): Boolean {
    val comp = getComponent(name, component) ?: return false
    client?.interfaceItem(comp["parent", -1], comp.id, item, amount)
    return true
}

fun Player.open(interfaceName: String): Boolean {
    val defs: InterfaceDefinitions = get()
    val type = defs.get(interfaceName)["type", ""]
    if (type.isNotEmpty()) {
        val id = interfaces.get(type)
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