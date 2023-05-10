package world.gregs.voidps.engine.client

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.*

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
    name: String? = null
) {
    if (this !is Player) {
        return
    }
    client?.message(Colours.replaceCustomTags(text), type.id, tile, name, name?.toSnakeCase())
}


/**
 * Sends a list of items to display on an interface item group component
 * @param container The id of the container
 * @param size The capacity of items in the container
 * @param items List of the item ids to display
 * @param primary Optional to send to the primary or secondary container
 */
fun Player.sendContainerItems(
    container: Int,
    size: Int,
    items: IntArray,
    primary: Boolean
) = client?.sendContainerItems(container, size, items, primary) ?: Unit

/**
 * Sends a list of items to display on an interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param secondary Optional to send to the primary or secondary container
 */
fun Player.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    secondary: Boolean
) = client?.sendInterfaceItemUpdate(key, updates, secondary) ?: Unit

/**
 * Sends settings to an interface's component(s)
 * @param id The id of the parent window
 * @param component The index of the component
 * @param fromSlot The start slot index
 * @param toSlot The end slot index
 * @param settings The settings hash
 */
fun Player.sendInterfaceSettings(
    id: Int,
    component: Int,
    fromSlot: Int,
    toSlot: Int,
    settings: Int
) = client?.sendInterfaceSettings(
    id,
    component,
    fromSlot,
    toSlot,
    settings
) ?: Unit

/**
 * Sends vertical height to an interfaces' component
 * @param id The id of the parent window
 * @param component The index of the component
 * @param settings The settings hash
 */
fun Player.sendInterfaceScroll(
    id: Int,
    component: Int,
    settings: Int
) = client?.sendInterfaceScroll(id, component, settings) ?: Unit

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
    id: Int,
    vararg params: Any
) = sendScript(id, params.toList())

fun Player.sendScript(
    id: Int,
    params: List<Any>
) = client?.sendScript(id, params) ?: Unit

fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.playMusicTrack(music, delay, volume) ?: Unit

fun Player.privateStatus(
    private: String
) {
    client?.sendPrivateStatus(when (private) {
        "friends" -> 1
        "off" -> 2
        else -> 0
    })
}

fun Player.publicStatus(
    public: String,
    trade: String
) {
    client?.sendPublicStatus(when (public) {
        "friends" -> 1
        "off" -> 2
        "hide" -> 3
        else -> 0
    }, when (trade) {
        "friends" -> 1
        "off" -> 2
        else -> 0
    })
}

fun Player.updateFriend(friend: Friend) = client?.sendFriendsList(listOf(friend)) ?: Unit

fun Player.moveCamera(
    tile: Tile,
    height: Int,
    constantSpeed: Int = 232,
    variableSpeed: Int = 232,
) {
    val viewport = viewport ?: return
    val result = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
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
    val result = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
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