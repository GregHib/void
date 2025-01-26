package world.gregs.voidps.engine.client

import net.pearx.kasechange.toSnakeCase
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
 * Sends a message to the client with specific chat settings and formatting.
 *
 * @param text The content of the message that will be sent.
 * @param type The type of chat message to display, default is ChatType.Game.
 * @param tile The tile ID associated with the message, default is 0.
 * @param name Optional name associated with the message, default is null.
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
    getOrPut("messages") { FixedSizeQueue<String>(100) }.add(text)
    val font = get<FontDefinitions>().get("p12_full")
    for (line in font.splitLines(Colours.replaceCustomTags(text), 484)) {
        client?.message(line, type.id, tile, name, name?.toSnakeCase())
    }
}

/**
 * A fixed-size queue that only retains the most recent elements added, up to a specified capacity.
 * This class extends LinkedList and automatically removes the oldest element when the capacity is reached.
 *
 * @param E The type of elements held in this queue.
 * @param capacity The maximum number of elements the queue can hold.
 */
private class FixedSizeQueue<E>(private val capacity: Int) : LinkedList<E>() {
    /**
     * Adds the specified element to the collection. If the collection has reached its maximum capacity,
     * the first element in the collection is removed before adding the new element.
     *
     * @param element the element to be added to the collection
     * @return `true` if the collection was modified as a result of the call, `false` otherwise
     */
    override fun add(element: E): Boolean {
        if (size >= capacity) {
            removeFirst()
        }
        return super.add(element)
    }
}

/**
 * Sends inventory items to the client for display or processing.
 *
 * @param inventory The ID of the inventory to which the items belong.
 * @param size The number of slots in the inventory.
 * @param items An array containing the item IDs in the inventory.
 * @param primary A flag indicating if this inventory is the primary one.
 */
fun Player.sendInventoryItems(
    inventory: Int,
    size: Int,
    items: IntArray,
    primary: Boolean
) = client?.sendInventoryItems(inventory, size, items, primary) ?: Unit

/**
 * Sends an update for an interface item.
 *
 * @param key The identifier for the interface to update.
 * @param updates A list of triples, where each triple contains the item ID, amount, and slot.
 * @param secondary A boolean value indicating whether this is a secondary update or not.
 */
fun Player.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    secondary: Boolean
) = client?.sendInterfaceItemUpdate(key, updates, secondary) ?: Unit

/**
 * Sends interface settings to the client for a specific interface component.
 *
 * @param id The ID of the interface.
 * @param component The ID of the component within the interface.
 * @param fromSlot The starting slot of the range for which the settings apply.
 * @param toSlot The ending slot of the range for which the settings apply.
 * @param settings The settings to apply to the specified interface component and slot range.
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
 * Sends an interface scroll event to the client associated with the player.
 *
 * @param id The unique identifier of the interface.
 * @param component The specific component within the interface.
 * @param settings Additional settings related to the interface scroll.
 */
fun Player.sendInterfaceScroll(
    id: Int,
    component: Int,
    settings: Int
) = client?.sendInterfaceScroll(id, component, settings) ?: Unit

/**
 * Sends the player's current run energy to the client.
 *
 * @param energy The current run energy value to be sent to the client.
 */
fun Player.sendRunEnergy(energy: Int) = client?.sendRunEnergy(energy) ?: Unit

/**
 * Sends a client script to the player with the specified script ID and parameters.
 *
 * @param id The unique identifier of the client script to send.
 * @param params The parameters to be passed to the client script.
 */
fun Player.sendScript(
    id: String,
    vararg params: Any?
) {
    val definition = get<ClientScriptDefinitions>().get(id)
    sendScript(definition.id, params.toList())
}

/**
 * Sends a script to the player using the specified script ID and parameters.
 *
 * @param id The unique identifier of the script to be sent.
 * @param params A list of parameters to be passed to the script. Nullable values are allowed.
 */
fun Player.sendScript(
    id: Int,
    params: List<Any?>
) = client?.sendScript(id, params) ?: Unit

/**
 * Plays a music track using the Player's audio system.
 *
 * @param music The identifier of the music track to be played.
 * @param delay The delay in milliseconds before the music track starts playing. Default is 100.
 * @param volume The volume level of the music track, where 255 is the maximum volume. Default is 255.
 */
fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.playMusicTrack(music, delay, volume) ?: Unit

/**
 * Updates the private status of the player based on the given parameter.
 *
 * @param private A string representing the private status. Possible values are:
 * - "friends" to set the private status to friends-only.
 * - "off" to turn the private status off.
 * - Any other value sets the private status to default.
 */
fun Player.privateStatus(
    private: String
) {
    client?.sendPrivateStatus(when (private) {
        "friends" -> 1
        "off" -> 2
        else -> 0
    })
}

/**
 * Updates the public and trade status of the player.
 *
 * @param public The public status of the player. Expected values are:
 *               "friends", "off", "hide", or other values default to 0.
 * @param trade The trade status of the player. Expected values are:
 *              "friends", "off", or other values default to 0.
 */
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

/**
 * Updates the friend's information in the friends list of the player.
 *
 * @param friend The Friend instance containing the updated data to be sent to the friends list.
 */
fun Player.updateFriend(friend: Friend) = client?.sendFriendsList(listOf(friend)) ?: Unit

/**
 * Moves the camera to a specified position with defined parameters.
 *
 * @param tile The target tile position to move the camera to.
 * @param height The height level for the camera.
 * @param constantSpeed The constant speed at which the camera moves. Default is 232.
 * @param variableSpeed The variable speed at which the camera moves. Default is 232.
 */
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

/**
 * Turns the player's camera to a specific tile position with adjustable speed settings.
 *
 * @param tile The target tile to which the camera should be turned.
 * @param height The vertical height of the camera focus in relation to the target tile.
 * @param constantSpeed The constant speed at which the camera moves (default is 232).
 * @param variableSpeed The variable speed for camera movement calculation (default is 232).
 */
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

/**
 * Applies a camera shake effect for the player.
 *
 * @param intensity The intensity of the camera shake.
 * @param type The type of shake applied to the camera.
 * @param cycle The duration or cycle of the camera shake effect.
 * @param movement The movement amount or amplitude of the shake.
 * @param speed The speed of the camera shake effect.
 */
fun Player.shakeCamera(
    intensity: Int,
    type: Int,
    cycle: Int,
    movement: Int,
    speed: Int,
) = client?.shakeCamera(intensity, type, cycle, movement, speed) ?: Unit

/**
 * Clears the camera settings for the player.
 *
 * This method resets the camera state for the player, if a client is associated
 * with the player. If no client is present, the method performs no operation.
 */
fun Player.clearCamera() = client?.clearCamera() ?: Unit

/**
 * Enum class that represents the different states or modes of a minimap.
 *
 * @property index Represents the unique integer value associated with each minimap state.
 */
enum class Minimap(val index: Int) {
    /**
     * Represents an enumeration value that signifies an unclickable state.
     * This could be utilized in settings or scenarios where a clickable functionality needs
     * to be intentionally disabled or marked as inactive.
     */
    Unclickable(1),
    /**
     * This is a class that represents a map with hidden elements or functionality.
     *
     * The primary purpose of the HideMap class is to define and manage a
     * map with specific hidden or obfuscated features.
     *
     * @constructor Creates a HideMap object with the given parameters.
     */
    HideMap(2),
    /**
     * Enumeration constant used to represent the visibility state of a compass indicator.
     *
     * @constructor Creates an instance of the `HideCompass` enum with the specified value for managing the compass visibility state.
     * @param value An integer value representing a specific state for hiding or showing the compass.
     */
    HideCompass(3),
}

/**
 * Updates the minimap state for the player by combining the provided states and sending the result
 * to the player's client.
 *
 * @param states Vararg parameter representing the states to be applied to the minimap. Each state is
 * represented by a Minimap instance, whose index will be used for bitwise operations.
 */
fun Player.minimap(vararg states: Minimap) {
    client?.sendMinimapState(states.fold(0) { acc, state -> acc or state.index })
}

/**
 * Clears the minimap display for the player by resetting its state.
 *
 * This method sends a reset instruction to the player's client, causing
 * the minimap to be cleared or reverted to its default state. If the
 * player's client is unavailable, the operation safely performs no action.
 */
fun Player.clearMinimap() = client?.sendMinimapState(0) ?: Unit