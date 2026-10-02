package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.smart
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.WORLD_LIST_FULL
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeShort
import world.gregs.voidps.network.login.protocol.writeSmart
import world.gregs.voidps.network.login.protocol.writeText

/**
 * World flags
 */
object WorldFlag {
    const val MEMBERS = 0x1
    const val PVP = 0x4
    const val BOUNTY = 0x20
}

/**
 * Sends a world list containing only the current world.
 * Signed clients look up the current world's flags from this list (cs2 6506), without it every flag
 * is treated as set making the client think it's on a pvp/bounty world (e.g. showing the wilderness level)
 * @param flags [WorldFlag]s
 */
fun Client.sendWorldList(
    world: Int,
    flags: Int,
    activity: String = "",
    address: String = "",
    players: Int = 0,
    country: Int = 0,
    location: String = "",
) {
    val size = 3 + smart(1) + smart(country) + 1 + string(location) +
        smart(world) * 2 + smart(1) + // Id range and count
        smart(0) + 5 + 1 + string(activity) + 1 + string(address) + // World
        4 + smart(0) + 2 // Checksum and player counts
    send(WORLD_LIST_FULL, size, Client.SHORT) {
        writeByte(true) // Complete
        writeByte(2) // Version
        writeByte(true) // Includes world info
        writeSmart(1) // Location count
        writeSmart(country)
        writeVersionedText(location)
        writeSmart(world) // Lowest world id
        writeSmart(world) // Highest world id
        writeSmart(1) // World count
        writeSmart(0) // World offset
        writeByte(0) // Location index
        writeInt(flags)
        writeVersionedText(activity)
        writeVersionedText(address)
        writeInt(0) // Checksum
        writeSmart(0) // World offset
        writeShort(players)
    }
}

private suspend fun ByteWriteChannel.writeVersionedText(value: String) {
    writeByte(0)
    writeText(value)
}
