package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.name
import world.gregs.voidps.network.Client.Companion.smart
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.CHAT
import world.gregs.voidps.network.Protocol.PRIVATE_CHAT_FROM
import world.gregs.voidps.network.Protocol.PUBLIC_CHAT
import world.gregs.voidps.network.Protocol.UNKNOWN_17
import world.gregs.voidps.network.Protocol.UNKNOWN_53
import world.gregs.voidps.network.Protocol.UNKNOWN_58

/**
 * A chat box message to display
 * @param type The message type
 * @param tile The tile the message was sent from
 * @param name Optional display name?
 * @param text The chat message text
 */
fun Client.message(text: String, type: Int, tile: Int = 0, name: String? = null, formatted: String?) {
    val mask = getMask(name, formatted)
    send(CHAT, getLength(type, text, name, mask, formatted), BYTE) {
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

// private message from
fun Client.privateChatFrom(accountName: String, displayName: String, rights: Int, data: ByteArray) {
    send(PRIVATE_CHAT_FROM, name(accountName, displayName) + 6 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeRandom()
        writeByte(rights)
        writeBytes(data)
    }
}

// Blank message
fun Client.packet30(accountName: String, displayName: String, rights: Int, data: ByteArray) {
    send(UNKNOWN_58, name(accountName, displayName) + 1 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeByte(rights)
        writeBytes(data)
    }
}

// private quick chat from?
fun Client.privateQuickChat(accountName: String, displayName: String, rights: Int, file: Int, data: ByteArray) {
    send(UNKNOWN_17, name(accountName, displayName) + 8 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeRandom()
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}

// public quick chat?
fun Client.packet21(accountName: String, displayName: String, rights: Int, file: Int, data: ByteArray) {
    send(UNKNOWN_53, name(accountName, displayName) + 3 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}