package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Client.Companion.name
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.APPEND_CLAN_CHAT
import world.gregs.voidps.network.Protocol.CLAN_CHAT
import world.gregs.voidps.network.Protocol.CLAN_QUICK_CHAT
import world.gregs.voidps.network.Protocol.UPDATE_CLAN_CHAT

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

data class Member(val accountName: String, val displayName: String, val world: Int, val rank: Int, val worldName: String)

fun Client.appendClanChat(member: Member) {
    send(APPEND_CLAN_CHAT, count(member), BYTE) {
        writeMember(member)
    }
}

fun Client.updateClanChat(owner: String, ownerDisplay: String, clan: String, kickRank: Int, members: List<Member>) {
    val different = owner != ownerDisplay
    send(UPDATE_CLAN_CHAT, string(owner) + (if (different) string(ownerDisplay) else 0) + 11 + members.sumOf { count(it) }, SHORT) {
        writeString(owner)
        writeByte(different)
        if (different) {
            writeString(ownerDisplay)
        }
        writeLong(clan)
        writeByte(kickRank)
        writeByte(members.size)
        members.forEach { member ->
            writeMember(member)
        }
    }
}

private fun count(member: Member): Int {
    var count = 4 + string(member.accountName) + string(member.worldName)
    if (member.accountName != member.displayName) {
        count += string(member.displayName)
    }
    return count
}

private suspend fun ByteWriteChannel.writeMember(member: Member) {
    writeString(member.accountName)
    val different = member.accountName != member.displayName
    writeByte(different)
    if (different) {
        writeString(member.displayName)
    }
    writeShort(member.world)
    writeByte(member.rank)
    writeString(member.worldName)
}