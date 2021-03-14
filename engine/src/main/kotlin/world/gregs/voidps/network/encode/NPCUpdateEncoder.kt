package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.writeBytes
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.NPC_UPDATING
import world.gregs.voidps.network.PacketSize

fun Client.updateNPCs(
    changes: BufferWriter,
    updates: BufferWriter
) = send(NPC_UPDATING, changes.position() + updates.position(), PacketSize.SHORT) {
    writeBytes(changes.toArray())
    writeBytes(updates.toArray())
    changes.clear()
    updates.clear()
}