package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*

fun Client.login(username: String, index: Int, rights: Int) = send(-1) {
    writeByte(Response.SUCCESS)
    writeByte(13 + Client.string(username))
    writeByte(rights)
    writeByte(0)// Unknown - something to do with skipping chat messages
    writeByte(0)
    writeByte(0)
    writeByte(0)
    writeByte(0)// Moves chat box position
    writeShort(index)
    writeByte(true)
    writeMedium(0)
    writeByte(true)
    writeString(username)
    flush()
}