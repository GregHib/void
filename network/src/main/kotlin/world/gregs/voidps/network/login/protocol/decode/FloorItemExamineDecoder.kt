package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.login.protocol.*

class FloorItemExamineDecoder : Decoder(7) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val id = packet.g2Alt2()
        val run = packet.readBoolean()
        val y = packet.readShort().toInt()
        val x = packet.g2Alt1()
        return ExamineItem(id)
    }

}