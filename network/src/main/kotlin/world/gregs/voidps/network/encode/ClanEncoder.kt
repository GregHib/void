package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.name
import world.gregs.voidps.network.Protocol.CLAN_CHAT
import world.gregs.voidps.network.Protocol.CLAN_QUICK_CHAT

fun Client.clanChat(accountName: String, displayName: String, clan: String, rights: Int, data: ByteArray) {
    send(CLAN_CHAT, name(accountName, displayName) + 14 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeLong(clan)
        writeRandom()
        writeByte(rights)
        writeBytes(data)
    }
}

fun Client.clanQuickChat(accountName: String, displayName: String, clan: String, rights: Int, file: Int, data: ByteArray) {
    send(CLAN_QUICK_CHAT, name(accountName, displayName) + 16 + data.size, BYTE) {
        writeName(accountName, displayName)
        writeLong(clan)
        writeRandom()
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}