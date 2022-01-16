package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.name
import world.gregs.voidps.network.Client.Companion.smart
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.GAME_MESSAGE
import world.gregs.voidps.network.Protocol.PRIVATE_CHAT_FROM
import world.gregs.voidps.network.Protocol.PRIVATE_CHAT_TO
import world.gregs.voidps.network.Protocol.PRIVATE_QUICK_CHAT_FROM
import world.gregs.voidps.network.Protocol.PRIVATE_QUICK_CHAT_TO
import world.gregs.voidps.network.Protocol.PUBLIC_CHAT

/**
 * A chat box message to display
 * @param type The message type
 * @param tile The tile the message was sent from
 * @param name Optional display name?
 * @param text The chat message text
 */
fun Client.message(text: String, type: Int, tile: Int = 0, name: String? = null, formatted: String?) {
    val mask = getMask(name, formatted)
    send(GAME_MESSAGE, getLength(type, text, name, mask, formatted), BYTE) {
        writeSmart(type)
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

fun Client.publicChat(message: ByteArray, index: Int, effects: Int, rights: Int) {
    send(PUBLIC_CHAT, message.size + 5, BYTE) {
        writeShort(index)
        writeShort(effects)
        writeByte(rights)
        writeBytes(message)
    }
}

fun Client.publicQuickChat(index: Int,  effects: Int, rights: Int, file: Int, data: ByteArray) {
    send(PUBLIC_CHAT, data.size + 7, BYTE) {
        writeShort(index)
        writeShort(effects)
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}

fun Client.privateChatFrom(displayName: String, rights: Int, data: ByteArray, responseName: String = displayName) {
    send(PRIVATE_CHAT_FROM, name(displayName, responseName) + 6 + data.size, BYTE) {
        writeName(displayName, responseName)
        writeRandom()
        writeByte(rights)
        writeBytes(data)
    }
}

fun Client.privateQuickChatFrom(displayName: String, rights: Int, file: Int, data: ByteArray, responseName: String = displayName) {
    send(PRIVATE_QUICK_CHAT_FROM, name(displayName, responseName) + 8 + data.size, BYTE) {
        writeName(displayName, responseName)
        writeRandom()
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}

fun Client.privateChatTo(displayName: String, data: ByteArray) {
    send(PRIVATE_CHAT_TO, string(displayName) + data.size, BYTE) {
        writeString(displayName)
        writeBytes(data)
    }
}

fun Client.privateQuickChatTo(displayName: String, file: Int, data: ByteArray) {
    send(PRIVATE_QUICK_CHAT_TO, string(displayName) + 2 + data.size, BYTE) {
        writeString(displayName)
        writeShort(file)
        writeBytes(data)
    }
}