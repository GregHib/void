package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanSubtract

class FloorItemOption1Decoder : Decoder(7) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val id = packet.readShort().toInt()
        val x = packet.readShort().toInt()
        val y = packet.readShort().toInt()
        val run = packet.readBooleanSubtract()
        return InteractFloorItem(id, x, y, 0)
    }
}
