package world.gregs.voidps.engine.client

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.utility.get
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
fun Player.message(
    text: String,
    type: ChatType = ChatType.Game,
    tile: Int = 0,
    name: String? = null
) = client?.message(text, type.id, tile, name, name?.toSnakeCase()) ?: Unit


/**
 * Sends a list of items to display on an interface item group component
 * @param container The id of the container
 * @param items List of the item ids to display
 * @param amounts List of the item amounts to display
 * @param primary Optional to send to the primary or secondary container
 */
fun Player.sendContainerItems(
    container: Int,
    items: IntArray,
    amounts: IntArray,
    primary: Boolean
) = client?.sendContainerItems(container, items, amounts, primary) ?: Unit

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


/**
 * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
 * @param id The file id
 * @param value The value to pass to the config file
 */
fun Player.sendVarbit(id: Int, value: Int) = client?.sendVarbit(id, value) ?: Unit

/**
 * Client variable; also known as "ConfigGlobal"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarc(id: Int, value: Int) = client?.sendVarc(id, value) ?: Unit

/**
 * Client variable; also known as "GlobalString"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarcStr(id: Int, value: String) = client?.sendVarcStr(id, value) ?: Unit

/**
 * A variable player config; also known as "Config", known in the client as "clientvarp"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarp(id: Int, value: Int) = client?.sendVarp(id, value) ?: Unit

fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.playMusicTrack(music, delay, volume) ?: Unit

fun String.compress(): ByteArray {
    val data = BufferWriter(128)
    get<Huffman>().compress(this, data)
    return data.toArray()
}

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