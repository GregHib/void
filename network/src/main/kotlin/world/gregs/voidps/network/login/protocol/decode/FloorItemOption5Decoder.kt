package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.*

class FloorItemOption5Decoder : Decoder(7) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val id = packet.g2Alt2()
        val run = packet.readBoolean()
        val y = packet.readShort().toInt()
        val x = packet.readShort().toInt()
        return InteractFloorItem(id, x, y, 4)
    }

}