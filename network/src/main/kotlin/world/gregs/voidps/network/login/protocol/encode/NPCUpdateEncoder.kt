package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.login.Protocol.NPC_UPDATING
import world.gregs.voidps.network.login.protocol.writeBytes

fun Client.updateNPCs(
    changes: ArrayWriter,
    updates: ArrayWriter,
) = send(NPC_UPDATING, changes.position() + updates.position(), SHORT) {
    writeBytes(changes.toArray())
    writeBytes(updates.toArray())
}
