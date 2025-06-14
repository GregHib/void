package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.BYTE
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.name
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.APPEND_CLAN_CHAT
import world.gregs.voidps.network.login.Protocol.CLAN_CHAT
import world.gregs.voidps.network.login.Protocol.CLAN_QUICK_CHAT
import world.gregs.voidps.network.login.Protocol.UPDATE_CLAN_CHAT
import world.gregs.voidps.network.login.protocol.*

fun Client.clanChat(displayName: String, clan: String, rights: Int, data: ByteArray, responseName: String = displayName) {
    send(CLAN_CHAT, name(displayName, responseName) + 14 + data.size, BYTE) {
        writeName(displayName, responseName)
        writeLong(clan)
        writeRandom()
        writeByte(rights)
        writeBytes(data)
    }
}

fun Client.clanQuickChat(displayName: String, clan: String, rights: Int, file: Int, data: ByteArray, responseName: String = displayName) {
    send(CLAN_QUICK_CHAT, name(displayName, responseName) + 16 + data.size, BYTE) {
        writeName(displayName, responseName)
        writeLong(clan)
        writeRandom()
        writeByte(rights)
        writeShort(file)
        writeBytes(data)
    }
}

data class Member(val displayName: String, val world: Int, val rank: Int, val worldName: String, val responseName: String = displayName)

fun Client.appendClanChat(member: Member) {
    send(APPEND_CLAN_CHAT, count(member), BYTE) {
        writeMember(member)
    }
}

fun Client.updateClanChat(displayName: String, clan: String, kickRank: Int, members: List<Member>, responseName: String = displayName) {
    val different = displayName != responseName
    send(UPDATE_CLAN_CHAT, string(displayName) + (if (different) string(responseName) else 0) + 11 + members.sumOf { count(it) }, SHORT) {
        writeString(displayName)
        writeByte(different)
        if (different) {
            writeString(responseName)
        }
        writeLong(clan)
        writeByte(kickRank)
        writeByte(members.size)
        members.forEach { member ->
            writeMember(member)
        }
    }
}

fun Client.leaveClanChat() = send(UPDATE_CLAN_CHAT, 0, SHORT) {}

private fun count(member: Member): Int {
    var count = 4 + string(member.displayName) + string(member.worldName)
    if (member.displayName != member.responseName) {
        count += string(member.responseName)
    }
    return count
}

private suspend fun ByteWriteChannel.writeMember(member: Member) {
    writeString(member.displayName)
    val different = member.displayName != member.responseName
    writeByte(different)
    if (different) {
        writeString(member.responseName)
    }
    writeShort(member.world)
    writeByte(member.rank)
    writeString(member.worldName)
}
