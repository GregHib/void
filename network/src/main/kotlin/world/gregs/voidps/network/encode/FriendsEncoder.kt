package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.UPDATE_FRIENDS
import world.gregs.voidps.network.writeByte
import world.gregs.voidps.network.writeString

fun Client.updateFriendsList(renamed: Boolean, name: String, previousName: String, rank: Int, world: Int, worldName: String = "", gameQuickChat: Boolean = true) {
    send(UPDATE_FRIENDS, string(name) + string(previousName) + (if (worldName.isNotEmpty()) string(worldName) + 1 else 0) + 4, Client.SHORT) {
        writeByte(renamed)
        writeString(name)
        writeString(previousName)
        writeShort(world)
        writeByte(rank)
        if (worldName.isNotEmpty()) {
            writeString(worldName)
            writeByte(gameQuickChat)
        }
    }
}