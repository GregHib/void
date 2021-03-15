package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.GameOpcodes.PLAYER_UPDATING
import world.gregs.voidps.network.writeBytes

fun Client.updatePlayers(
    changes: BufferWriter,
    updates: BufferWriter
) = send(PLAYER_UPDATING, changes.position() + updates.position(), SHORT) {
    writeBytes(changes.toArray())
    writeBytes(updates.toArray())
    changes.clear()
    updates.clear()
}