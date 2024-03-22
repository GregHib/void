package world.gregs.voidps.network.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.Protocol.INTERFACE_ANIMATION
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.smart
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.protocol.*

/**
 * Sends an animation to an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param animation The animation id
 */
fun Client.animateInterface(
    id: Int,
    component: Int,
    animation: Int
) = send(INTERFACE_ANIMATION) {
    writeShort(animation)
    writeIntMiddle(id shl 16 or component)
}

/**
 * Closes a client interface
 * @param id The id of the parent interface
 * @param component The index of the component to close
 */
fun Client.closeInterface(
    id: Int,
    component: Int
) = send(Protocol.INTERFACE_CLOSE) {
    writeInt(id shl 16 or component)
}

/**
 * Sends a colour to an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param red red value out of 32
 * @param green green value out of 32
 * @param blue blue value out of 32
 */
fun Client.colourInterface(
    id: Int,
    component: Int,
    red: Int,
    green: Int,
    blue: Int
) = colourInterface(id, component, (red shl 10) + (green shl 5) + blue)

/**
 * Sends a colour to an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 */
fun Client.colourInterface(
    id: Int,
    component: Int,
    colour: Int
) = send(Protocol.INTERFACE_COLOUR) {
    writeShortAdd(colour)
    writeIntLittle(id shl 16 or component)
}

/**
 * Sends npc who's head to display on an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param npc The id of the npc
 */
fun Client.npcDialogueHead(
    id: Int,
    component: Int,
    npc: Int
) = send(Protocol.INTERFACE_NPC_HEAD) {
    writeIntLittle(id shl 16 or component)
    writeShortAdd(npc)
}

/**
 * Sends command to display the players head on an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 */
fun Client.playerDialogueHead(
    id: Int,
    component: Int
) = send(Protocol.INTERFACE_PLAYER_HEAD) {
    writeIntMiddle(id shl 16 or component)
}

/**
 * Sends an item to display on an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param item The item id
 * @param amount The number of the item
 */
fun Client.interfaceItem(
    id: Int,
    component: Int,
    item: Int,
    amount: Int
) = send(Protocol.INTERFACE_ITEM) {
    writeShortLittle(item)
    writeIntInverseMiddle(id shl 16 or component)
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
    secondary: Boolean
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

private fun getLength(updates: List<Triple<Int, Int, Int>>): Int {
    return 3 + updates.sumOf { (index, item, amount) -> smart(index) + if (item >= 0) if (amount >= 255) 7 else 3 else 2 }
}

/**
 * Displays an interface onto the client screen
 * @param permanent Whether the interface should be removed on player movement
 * @param parent The id of the parent interface
 * @param component The index of the component
 * @param id The id of the interface to display
 */
fun Client.openInterface(
    permanent: Boolean,
    parent: Int,
    component: Int,
    id: Int
) = send(Protocol.INTERFACE_OPEN) {
    writeShortLittle(id)
    writeIntLittle(parent shl 16 or component)
    writeByteAdd(permanent)
}

/**
 * Sends settings to an interface's component(s)
 * @param id The id of the parent window
 * @param component The index of the component
 * @param fromSlot The start slot index
 * @param toSlot The end slot index
 * @param settings The settings hash
 */
fun Client.sendInterfaceSettings(
    id: Int,
    component: Int,
    fromSlot: Int,
    toSlot: Int,
    settings: Int
) = send(Protocol.INTERFACE_COMPONENT_SETTINGS) {
    writeShortAdd(toSlot)
    writeShortLittle(fromSlot)
    writeInt(id shl 16 or component)
    writeIntInverseMiddle(settings)
}

/**
 * Sends vertical height to an interfaces' component
 * @param id The id of the parent window
 * @param component The index of the component
 * @param settings The settings hash
 */
fun Client.sendInterfaceScroll(
    id: Int,
    component: Int,
    settings: Int
) = send(Protocol.INTERFACE_SCROLL_VERTICAL) {
    writeIntInverseMiddle(id shl 16 or component)
    writeShortAdd(settings)
}

/**
 * Sends a sprite to an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param sprite The sprite id
 */
fun Client.interfaceSprite(
    id: Int,
    component: Int,
    sprite: Int
) = send(Protocol.INTERFACE_SPRITE) {
    writeShortAdd(sprite)
    writeIntInverseMiddle(id shl 16 or component)
}

/**
 * Update the text of an interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param text The text to send
 */
fun Client.interfaceText(
    id: Int,
    component: Int,
    text: String
) = send(Protocol.INTERFACE_TEXT, 4 + string(text), SHORT) {
    writeIntLittle(id shl 16 or component)
    writeString(text)
}


fun Client.updateInterface(
    id: Int,
    type: Int
) = send(Protocol.INTERFACE_WINDOW) {
    writeByteInverse(type)
    writeShortAdd(id)
}

/**
 * Toggles an interface component
 * @param id The parent interface id
 * @param component The component to change
 * @param hide Visibility
 */
fun Client.interfaceVisibility(
    id: Int,
    component: Int,
    hide: Boolean
) = send(Protocol.INTERFACE_COMPONENT_VISIBILITY) {
    writeByteAdd(hide)
    writeIntLittle(id shl 16 or component)
}