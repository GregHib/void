package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.playMusicTrack
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.Colour
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.*

/**
 * API for the interacting and tracking of client interfaces
 */
class Interfaces(
    private val player: Player,
    private val interfaces: MutableMap<String, String> = Object2ObjectOpenHashMap(),
) {
    var displayMode = 0

    var resizable: Boolean
        get() = displayMode >= RESIZABLE_SCREEN
        set(value) {
            displayMode = if (value) RESIZABLE_SCREEN else FIXED_SCREEN
        }

    val gameFrame: String
        get() = if (resizable) GAME_FRAME_RESIZE_NAME else GAME_FRAME_NAME

    fun open(id: String): Boolean {
        if (!hasOpenOrRootParent(id)) {
            return false
        }
        return sendIfOpened(id)
    }

    fun close(id: String?): Boolean {
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
        if (interfaces.remove(getType(id), id)) {
            sendClose(id)
            InterfaceApi.close(player, id)
            player.queue.clearWeak()
            return true
        }
        return false
    }

    fun get(type: String): String? = interfaces[type]

    fun contains(id: String): Boolean = interfaces[getType(id)] == id

    fun refresh() {
        for (id in interfaces.values) {
            sendOpen(id)
            notifyRefresh(id)
        }
    }

    private fun hasOpenOrRootParent(id: String): Boolean {
        val parent = InterfaceDefinitions.getOrNull(id)?.parent(resizable) ?: return false
        return parent == -1 || contains(InterfaceDefinitions.get(InterfaceDefinition.id(parent)).stringId)
    }

    private fun sendIfOpened(id: String): Boolean {
        val type = getType(id)
        if (interfaces[type] != id) {
            interfaces[type] = id
            sendOpen(id)
            InterfaceApi.open(player, id)
            notifyRefresh(id)
            return true
        }
        notifyRefresh(id)
        return false
    }

    private fun closeChildrenOf(parent: String) {
        val it = interfaces.iterator()
        val children = mutableListOf<String>()
        while (it.hasNext()) {
            val (_, id) = it.next()
            if (getParent(id) == parent) {
                it.remove()
                sendClose(id)
                InterfaceApi.close(player, id)
                (player as? Player)?.queue?.clearWeak()
                children.add(id)
            }
        }
        for (child in children) {
            closeChildrenOf(child)
        }
    }

    private fun getParent(id: String): String {
        val parent = InterfaceDefinitions.getOrNull(id)?.parent(resizable) ?: return ""
        return if (parent == -1) {
            ROOT_ID
        } else {
            InterfaceDefinitions.get(InterfaceDefinition.id(parent)).stringId
        }
    }

    private fun getType(id: String): String = InterfaceDefinitions.getOrNull(id)?.type ?: DEFAULT_TYPE

    private fun sendOpen(id: String) {
        val definition = InterfaceDefinitions.getOrNull(id) ?: return
        val parent = definition.parent(resizable)
        if (parent == -1) { // root
            player.client?.updateInterface(definition.id, 0)
        } else {
            player.client?.openInterface(
                permanent = definition.permanent,
                interfaceComponent = parent,
                id = definition.id,
            )
        }
    }

    private fun sendClose(id: String) {
        val parent = InterfaceDefinitions.getOrNull(id)?.parent(resizable)
        if (parent != null && parent != -1) {
            player.client?.closeInterface(parent)
        }
    }

    private fun notifyRefresh(id: String) {
        InterfaceApi.refresh(player, id)
    }

    fun sendAnimation(id: String, component: String, animation: Int): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        player.client?.animateInterface(comp.id, animation)
        return true
    }

    fun sendAnimation(id: String, component: String, animation: String): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        val definitions: AnimationDefinitions = get()
        player.client?.animateInterface(comp.id, definitions.get(animation).id)
        return true
    }

    fun sendText(id: String, component: String, text: String): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        player.client?.interfaceText(comp.id, Colours.replaceCustomTags(text))
        return true
    }

    fun sendVisibility(id: String, component: String, visible: Boolean): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        player.client?.interfaceVisibility(comp.id, !visible)
        return true
    }

    fun sendSprite(id: String, component: String, sprite: Int): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        player.client?.interfaceSprite(comp.id, sprite)
        return true
    }

    fun sendColour(id: String, component: String, colour: Colour): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        val red = (colour and 0xff0000) shr 16
        val green = (colour and 0xff00) shr 8
        val blue = colour and 0xff
        player.client?.colourInterface(comp.id, ((red / 255.0 * 31).toInt() shl 10) + ((green / 255.0 * 31).toInt() shl 5) + (blue / 255.0 * 31).toInt())
        return true
    }

    fun sendItem(id: String, component: String, item: Item): Boolean = sendItem(id, component, item.def.id, item.amount)

    fun sendItem(id: String, component: String, item: Int, amount: Int = 1): Boolean {
        val comp = InterfaceDefinitions.getComponent(id, component) ?: return false
        player.client?.interfaceItem(comp.id, item, amount)
        return true
    }

    fun setDisplayMode(displayMode: Int = 0): Boolean {
        val current = gameFrame
        if (contains(current)) {
            this.displayMode = displayMode
            remove(current)
            open(gameFrame)
            refresh()
            return true
        }
        return false
    }

    companion object {
        const val FIXED_SCREEN = 1
        const val RESIZABLE_SCREEN = 2
        const val FULL_SCREEN = 3

        const val GAME_FRAME_NAME = "toplevel"
        const val GAME_FRAME_RESIZE_NAME = "toplevel_full"
        const val ROOT_ID = "root"
        const val DEFAULT_TYPE = "main_screen"
        const val ROOT_INDEX = 0
    }
}

/**
 * @param close any interfaces open with the same type
 */
fun Player.open(interfaceId: String, close: Boolean = true): Boolean {
    val type = InterfaceDefinitions.getOrNull(interfaceId)?.type
    if (close && type != null) {
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
    sendScript("close_entry")
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
    this["playing_song"] = true
    this["current_track"] = trackIndex
}
