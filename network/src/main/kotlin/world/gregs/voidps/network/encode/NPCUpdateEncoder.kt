package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Protocol.NPC_UPDATING
import world.gregs.voidps.network.writeBytes

fun Client.updateNPCs(
    changes: BufferWriter,
    updates: BufferWriter
) = send(NPC_UPDATING, changes.position() + updates.position(), SHORT) {
    writeBytes(changes.toArray())
    writeBytes(updates.toArray())
}