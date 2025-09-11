package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.UPDATE_FRIENDS
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeShort
import world.gregs.voidps.network.login.protocol.writeString

data class Friend(
    val name: String,
    val previousName: String,
    val rank: Int = 0,
    val renamed: Boolean = false,
    val world: Int = 0,
    val worldName: String = "",
    val gameQuickChat: Boolean = true,
)

fun Client.sendFriendsList(friends: List<Friend>) {
    send(UPDATE_FRIENDS, friends.sumOf { count(it) }, Client.SHORT) {
        for (friend in friends) {
            writeFriend(friend)
        }
    }
}

private fun count(friend: Friend) = 4 + string(friend.name) + string(friend.previousName) + if (friend.world > 0) string(friend.worldName) + 1 else 0

private suspend fun ByteWriteChannel.writeFriend(friend: Friend) {
    writeByte(friend.renamed)
    writeString(friend.name)
    writeString(friend.previousName)
    writeShort(friend.world)
    writeByte(friend.rank)
    if (friend.world > 0) {
        writeString(friend.worldName)
        writeByte(friend.gameQuickChat)
    }
}
