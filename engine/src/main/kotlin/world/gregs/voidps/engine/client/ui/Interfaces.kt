package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.client.playMusicTrack
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
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
    internal var client: Client? = null,
    internal val definitions: InterfaceDefinitions,
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

    private fun getPermanent(id: String): Boolean {
        return definitions.get(id)["permanent", true]
    }

    private fun sendOpen(id: String) {
        val parent = getParent(id)
        if (parent == ROOT_ID) {
            client?.updateInterface(definitions.get(id).id, 0)
        } else {
            client?.openInterface(
                permanent = getPermanent(id),
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

    fun sendAnimation(id: String, component: String, animation: Int): Boolean {
        val comp = definitions.getComponent(id, component) ?: return false
        client?.animateInterface(comp["parent", -1], comp.id, animation)
        return true
    }

    fun sendText(id: String, component: String, text: String): Boolean {
        val comp = definitions.getComponent(id, component) ?: return false
        client?.interfaceText(comp["parent", -1], comp.id, Colours.replaceCustomTags(text))
        return true
    }

    fun sendVisibility(id: String, component: String, visible: Boolean): Boolean {
        val comp = definitions.getComponent(id, component) ?: return false
        client?.interfaceVisibility(comp["parent", -1], comp.id, !visible)
        return true
    }

    fun sendSprite(id: String, component: String, sprite: Int): Boolean {
        val comp = definitions.getComponent(id, component) ?: return false
        client?.interfaceSprite(comp["parent", -1], comp.id, sprite)
        return true
    }

    fun sendItem(id: String, component: String, item: Item): Boolean {
        return sendItem(id, component, item.def.id, item.amount)
    }

    fun sendItem(id: String, component: String, item: Int, amount: Int = 1): Boolean {
        val comp = definitions.getComponent(id, component) ?: return false
        client?.interfaceItem(comp["parent", -1], comp.id, item, amount)
        return true
    }

    companion object {
        const val ROOT_ID = "root"
        const val ROOT_INDEX = 0
    }
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

fun Character.hasMenuOpen(): Boolean {
    if (this !is Player) {
        return false
    }
    return hasTypeOpen("main_screen") || hasTypeOpen("wide_screen") || hasTypeOpen("underlay")
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
    get() = interfaces.get("main_screen") ?: interfaces.get("wide_screen") ?: interfaces.get("underlay")

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

fun Player.playTrack(trackIndex: Int) {
    val enums: EnumDefinitions = get()
    playMusicTrack(enums.get("music_tracks").getInt(trackIndex))
    val name = enums.get("music_track_names").getString(trackIndex)
    interfaces.sendText("music_player", "currently_playing", name)
    this["current_track"] = trackIndex
}