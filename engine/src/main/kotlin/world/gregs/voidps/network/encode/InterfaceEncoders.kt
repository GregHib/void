package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Client.Companion.smart
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.INTERFACE_ANIMATION

/**
 * Sends an animation to a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param animation The animation id
 */
fun Client.animateInterface(
    id: Int,
    component: Int,
    animation: Int
) = send(INTERFACE_ANIMATION, 6) {
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
) = send(Protocol.INTERFACE_CLOSE, 4) {
    writeInt(id shl 16 or component)
}

/**
 * Sends a sprite to a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 */
fun Client.colourInterface(
    id: Int,
    component: Int,
    red: Int,
    green: Int,
    blue: Int
) = send(Protocol.INTERFACE_COLOUR, 6) {
    writeShortAdd((red shl 10) + (green shl 5) + blue)
    writeIntLittle(id shl 16 or component)
}

/**
 * Sends npc who's head to display on a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param npc The id of the npc
 */
fun Client.npcDialogueHead(
    id: Int,
    component: Int,
    npc: Int
) = send(Protocol.INTERFACE_NPC_HEAD, 6) {
    writeIntLittle(id shl 16 or component)
    writeShortAdd(npc)
}

/**
 * Sends command to display the players head on a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 */
fun Client.playerDialogueHead(
    id: Int,
    component: Int
) = send(Protocol.INTERFACE_PLAYER_HEAD, 4) {
    writeIntMiddle(id shl 16 or component)
}

/**
 * Sends an item to display on a interface component
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
) = send(Protocol.INTERFACE_ITEM, 10) {
    writeShortLittle(item)
    writeIntInverseMiddle(id shl 16 or component)
    writeInt(amount)
}

/**
 * Sends a list of items to display on a interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param primary Optional to send to the primary or secondary container
 */
fun Player.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    primary: Boolean
) = client?.send(Protocol.INTERFACE_ITEMS_UPDATE, getLength(updates), SHORT) {
    writeShort(key)
    writeByte(primary)
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
    return 3 + updates.sumBy { (index, item, amount) -> smart(index) + if (item >= 0) if (amount >= 255) 7 else 3 else 2 }
}

/**
 * Displays a interface onto the client screen
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
) = send(Protocol.INTERFACE_OPEN, 7) {
    writeShortLittle(id)
    writeIntLittle(parent shl 16 or component)
    writeByteAdd(permanent)
}

/**
 * Sends settings to a interface's component(s)
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
) {
    client?.send(Protocol.INTERFACE_COMPONENT_SETTINGS, 12) {
        writeShortAdd(toSlot)
        writeShortLittle(fromSlot)
        writeInt(id shl 16 or component)
        writeIntInverseMiddle(settings)
    }
}

/**
 * Sends vertical height to a interfaces' component
 * @param id The id of the parent window
 * @param component The index of the component
 * @param settings The settings hash
 */
fun Player.sendInterfaceScroll(
    id: Int,
    component: Int,
    settings: Int
) {
    client?.send(Protocol.INTERFACE_SCROLL_VERTICAL, 6) {
        writeIntInverseMiddle(id shl 16 or component)
        writeShortAdd(settings)
    }
}

/**
 * Sends a sprite to a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param sprite The sprite id
 */
fun Client.interfaceSprite(
    id: Int,
    component: Int,
    sprite: Int
) = send(Protocol.INTERFACE_SPRITE, 6) {
    writeShortAdd(sprite)
    writeIntInverseMiddle(id shl 16 or component)
}

/**
 * Update the text of a interface component
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
) = send(Protocol.INTERFACE_WINDOW, 3) {
    writeByteInverse(type)
    writeShortAdd(id)
}

/**
 * Toggles a interface component
 * @param id The parent interface id
 * @param component The component to change
 * @param hide Visibility
 */
fun Client.interfaceVisibility(
    id: Int,
    component: Int,
    hide: Boolean
) = send(Protocol.INTERFACE_COMPONENT_VISIBILITY, 5) {
    writeByteAdd(hide)
    writeIntLittle(id shl 16 or component)
}