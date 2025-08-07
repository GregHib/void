package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.smart
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.Protocol.INTERFACE_ANIMATION
import world.gregs.voidps.network.login.protocol.*

/**
 * Sends an animation to an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param animation The animation id
 */
fun Client.animateInterface(
    interfaceComponent: Int,
    animation: Int,
) = send(INTERFACE_ANIMATION) {
    writeShort(animation)
    writeIntMiddle(interfaceComponent)
}

/**
 * Closes a client interface
 * @param interfaceComponent Packed component index and id of the parent window
 */
fun Client.closeInterface(
    interfaceComponent: Int,
) = send(Protocol.INTERFACE_CLOSE) {
    writeInt(interfaceComponent)
}

/**
 * Sends a colour to an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param red red value out of 32
 * @param green green value out of 32
 * @param blue blue value out of 32
 */
fun Client.colourInterface(
    interfaceComponent: Int,
    red: Int,
    green: Int,
    blue: Int,
) = colourInterface(interfaceComponent, (red shl 10) + (green shl 5) + blue)

/**
 * Sends a colour to an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param colour The colour to send
 */
fun Client.colourInterface(
    interfaceComponent: Int,
    colour: Int,
) = send(Protocol.INTERFACE_COLOUR) {
    writeShortAdd(colour)
    writeIntLittle(interfaceComponent)
}

/**
 * Sends npc who's head to display on an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param npc The id of the npc
 */
fun Client.npcDialogueHead(
    interfaceComponent: Int,
    npc: Int,
) = send(Protocol.INTERFACE_NPC_HEAD) {
    writeIntLittle(interfaceComponent)
    writeShortAdd(npc)
}

/**
 * Sends command to display the players head on an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 */
fun Client.playerDialogueHead(
    interfaceComponent: Int,
) = send(Protocol.INTERFACE_PLAYER_HEAD) {
    writeIntMiddle(interfaceComponent)
}

/**
 * Sends an item to display on an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param item The item id
 * @param amount The number of the item
 */
fun Client.interfaceItem(
    interfaceComponent: Int,
    item: Int,
    amount: Int,
) = send(Protocol.INTERFACE_ITEM) {
    writeShortLittle(item)
    writeIntInverseMiddle(interfaceComponent)
    writeInt(amount)
}

/**
 * Sends a list of items to display on an interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param secondary Optional to send to the primary or secondary inventory
 */
fun Client.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    secondary: Boolean,
) = send(Protocol.INTERFACE_ITEMS_UPDATE, getLength(updates), SHORT) {
    writeShort(key)
    writeByte(secondary)
    for ((index, item, amount) in updates) {
        writeSmart(index)
        writeShort(item + 1)
        if (item >= 0) {
            writeByte(if (amount >= 255) 255 else amount)
            if (amount >= 255) {
                writeInt(amount)
            }
        }
    }
}

private fun getLength(updates: List<Triple<Int, Int, Int>>): Int = 3 + updates.sumOf { (index, item, amount) -> smart(index) + if (item >= 0) if (amount >= 255) 7 else 3 else 2 }

/**
 * Displays an interface onto the client screen
 * @param permanent Whether the interface should be removed on player movement
 * @param interfaceComponent Packed component index and id of the parent window
 * @param id The id of the interface to display
 */
fun Client.openInterface(
    permanent: Boolean,
    interfaceComponent: Int,
    id: Int,
) = send(Protocol.INTERFACE_OPEN) {
    writeShortLittle(id)
    writeIntLittle(interfaceComponent)
    writeByteAdd(permanent)
}

/**
 * Sends settings to an interface's component(s)
 * @param interfaceComponent Packed component index and id of the parent window
 * @param fromSlot The start slot index
 * @param toSlot The end slot index
 * @param settings The settings hash
 */
fun Client.sendInterfaceSettings(
    interfaceComponent: Int,
    fromSlot: Int,
    toSlot: Int,
    settings: Int,
) = send(Protocol.INTERFACE_COMPONENT_SETTINGS) {
    writeShortAdd(toSlot)
    writeShortLittle(fromSlot)
    writeInt(interfaceComponent)
    writeIntInverseMiddle(settings)
}

/**
 * Sends vertical height to an interfaces' component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param settings The settings hash
 */
fun Client.sendInterfaceScroll(
    interfaceComponent: Int,
    settings: Int,
) = send(Protocol.INTERFACE_SCROLL_VERTICAL) {
    writeIntInverseMiddle(interfaceComponent)
    writeShortAdd(settings)
}

/**
 * Sends a sprite to an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param sprite The sprite id
 */
fun Client.interfaceSprite(
    interfaceComponent: Int,
    sprite: Int,
) = send(Protocol.INTERFACE_SPRITE) {
    writeShortAdd(sprite)
    writeIntInverseMiddle(interfaceComponent)
}

/**
 * Update the text of an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param text The text to send
 */
fun Client.interfaceText(
    interfaceComponent: Int,
    text: String,
) = send(Protocol.INTERFACE_TEXT, 4 + string(text), SHORT) {
    writeIntLittle(interfaceComponent)
    writeString(text)
}

fun Client.updateInterface(
    id: Int,
    type: Int,
) = send(Protocol.INTERFACE_WINDOW) {
    writeByteInverse(type)
    writeShortAdd(id)
}

/**
 * Toggles an interface component
 * @param interfaceComponent Packed component index and id of the parent window
 * @param hide Visibility
 */
fun Client.interfaceVisibility(
    interfaceComponent: Int,
    hide: Boolean,
) = send(Protocol.INTERFACE_COMPONENT_VISIBILITY) {
    writeByteAdd(hide)
    writeIntLittle(interfaceComponent)
}

/**
 * Send an update to grand exchange slot
 * @param slot offer slot
 * @param state the offer state 1 - submitting, 2 - open, 5 - cancelled
 * @param item item id
 * @param price price per item
 * @param amount quantity of items in offer
 * @param sold number of items sold so far
 * @param excess number of coins received
 */
fun Client.grandExchange(slot: Int, state: Int = 0, item: Int = 0, price: Int = 0, amount: Int = 0, sold: Int = 0, excess: Int = 0) = send(Protocol.GRAND_EXCHANGE_SLOT) {
    writeByte(slot)
    writeByte(state)
    writeShort(item)
    writeInt(price)
    writeInt(amount)
    writeInt(sold)
    writeInt(excess)
}
