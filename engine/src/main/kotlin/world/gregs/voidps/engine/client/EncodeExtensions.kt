package world.gregs.voidps.engine.client

import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.data.definition.ClientScriptDefinitions
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.*
import world.gregs.voidps.type.Tile
import java.util.*

/**
 * Helper functions to simplify common client calls
 */

/**
 * A chat box message to display
 * @param type The message type
 * @param tile The tile the message was sent from
 * @param name Optional display name?
 * @param text The chat message text
 */
fun Character.message(
    text: String,
    type: ChatType = ChatType.Game,
    tile: Int = 0,
    name: String? = null,
) {
    if (this !is Player) {
        return
    }
    getOrPut("messages") { FixedSizeQueue<String>(100) }.add(text)
    val font = get<FontDefinitions>().get("p12_full")
    for (line in font.splitLines(Colours.replaceCustomTags(text), 484)) {
        client?.message(line, type.id, tile, name, name?.lowercase(Locale.getDefault())?.replace(' ', '_'))
    }
}

private class FixedSizeQueue<E>(private val capacity: Int) : LinkedList<E>() {
    override fun add(element: E): Boolean {
        if (size >= capacity) {
            removeFirst()
        }
        return super.add(element)
    }
}

/**
 * Sends a list of items to display on an interface item group component
 * @param inventory The id of the inventory
 * @param size The capacity of items in the inventory
 * @param items List of the item ids to display
 * @param primary Optional to send to the primary or secondary inventory
 */
fun Player.sendInventoryItems(
    inventory: Int,
    size: Int,
    items: IntArray,
    primary: Boolean,
) = client?.sendInventoryItems(inventory, size, items, primary) ?: Unit

/**
 * Sends a list of items to display on an interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param secondary Optional to send to the primary or secondary inventory
 */
fun Player.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    secondary: Boolean,
) = client?.sendInterfaceItemUpdate(key, updates, secondary) ?: Unit

/**
 * Sends settings to an interface's component(s)
 * @param interfaceComponent Packed component index and id of the parent window
 * @param fromSlot The start slot index
 * @param toSlot The end slot index
 * @param settings The settings hash
 */
fun Player.sendInterfaceSettings(
    interfaceComponent: Int,
    fromSlot: Int,
    toSlot: Int,
    settings: Int,
) = client?.sendInterfaceSettings(
    interfaceComponent,
    fromSlot,
    toSlot,
    settings,
) ?: Unit

/**
 * Sends vertical height to an interfaces' component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param settings The settings hash
 */
fun Player.sendInterfaceScroll(
    interfaceComponent: Int,
    settings: Int,
) = client?.sendInterfaceScroll(interfaceComponent, settings) ?: Unit

/**
 * Sends run energy
 * @param energy The current energy value
 */
fun Player.sendRunEnergy(energy: Int) = client?.sendRunEnergy(energy) ?: Unit

/**
 * Sends a client script to run
 * @param id The client script id
 * @param params Additional parameters to run the script with (strings & integers only)
 */
fun Player.sendScript(
    id: String,
    vararg params: Any?,
) {
    val definition = get<ClientScriptDefinitions>().get(id)
    sendScript(definition.id, params.toList())
}

fun Player.sendScript(
    id: Int,
    params: List<Any?>,
) = client?.sendScript(id, params) ?: Unit

fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255,
) = client?.playMusicTrack(music, delay, volume) ?: Unit

fun Player.privateStatus(
    private: String,
) {
    client?.sendPrivateStatus(
        when (private) {
            "friends" -> 1
            "off" -> 2
            else -> 0
        },
    )
}

fun Player.publicStatus(
    public: String,
    trade: String,
) {
    client?.sendPublicStatus(
        when (public) {
            "friends" -> 1
            "off" -> 2
            "hide" -> 3
            else -> 0
        },
        when (trade) {
            "friends" -> 1
            "off" -> 2
            else -> 0
        },
    )
}

fun Player.moveCamera(
    tile: Tile,
    height: Int,
    constantSpeed: Int = 232,
    variableSpeed: Int = 232,
) {
    val viewport = viewport ?: return
    val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
    val local = tile.minus(result.tile)
    return client?.moveCamera(local.x, local.y, height, constantSpeed, variableSpeed) ?: Unit
}

fun Player.turnCamera(
    tile: Tile,
    height: Int,
    constantSpeed: Int = 232,
    variableSpeed: Int = 232,
) {
    val viewport = viewport ?: return
    val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
    val local = tile.minus(result.tile)
    return client?.turnCamera(local.x, local.y, height, constantSpeed, variableSpeed) ?: Unit
}

fun Player.shakeCamera(
    intensity: Int,
    type: Int,
    cycle: Int,
    movement: Int,
    speed: Int,
) = client?.shakeCamera(intensity, type, cycle, movement, speed) ?: Unit

fun Player.clearCamera() = client?.clearCamera() ?: Unit

enum class Minimap(val index: Int) {
    Unclickable(1),
    HideMap(2),
    HideCompass(3),
}

fun Player.minimap(vararg states: Minimap) {
    client?.sendMinimapState(states.fold(0) { acc, state -> acc or state.index })
}

fun Player.clearMinimap() = client?.sendMinimapState(0) ?: Unit
