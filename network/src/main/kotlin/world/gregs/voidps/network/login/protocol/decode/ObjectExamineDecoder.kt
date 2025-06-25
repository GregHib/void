package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineObject
import world.gregs.voidps.network.login.protocol.*

class ObjectExamineDecoder : Decoder(7) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.readBooleanAdd()
        val x = packet.readUnsignedShortAdd()
        val objectId = packet.readUnsignedShortAddLittle()
        val y = packet.g2Alt1()
        return ExamineObject(objectId)
    }

}