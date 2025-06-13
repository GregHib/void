package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.login.Protocol.PLAYER_UPDATING
import world.gregs.voidps.network.login.protocol.writeBytes

fun Client.updatePlayers(
    changes: BufferWriter,
    updates: BufferWriter,
) = send(PLAYER_UPDATING, changes.position() + updates.position(), SHORT) {
    writeBytes(changes.toArray())
    writeBytes(updates.toArray())
}
