package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.smart
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.CHAT
import world.gregs.voidps.network.writeSmart
import world.gregs.voidps.network.writeString

/**
 * A chat box message to display
 * @param type The message type
 * @param tile The tile the message was sent from
 * @param name Optional display name?
 * @param text The chat message text
 */
fun Player.message(text: String, type: ChatType = ChatType.Game, tile: Int = 0, name: String? = null) {
    val formatted = name?.toLowerCase()?.replace(" ", "_")
    val mask = getMask(name, formatted)
    client?.send(CHAT, getLength(type.id, text, name, mask, formatted), BYTE) {
        writeSmart(type.id)
        writeInt(tile)
        writeByte(mask)
        if (name != null) {
            writeString(name)
            if (mask and 0x2 == 0x2) {
                writeString(formatted)
            }
        }
        writeString(text)
    }
}

private fun getMask(name: String?, formatted: String?): Int {
    var mask = 0
    if (name != null) {
        mask = mask or 0x1
        if (formatted != null && name != formatted) {
            mask = mask or 0x2
        }
    }
    return mask
}

private fun getLength(type: Int, message: String, name: String?, mask: Int, formatted: String?): Int {
    var length = smart(type) + 5 + string(message)
    if (name != null) {
        length += string(name)
        if (mask and 0x2 == 0x2) {
            length += string(formatted)
        }
    }
    return length
}