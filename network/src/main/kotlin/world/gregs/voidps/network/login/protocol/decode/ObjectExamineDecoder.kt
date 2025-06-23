package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineObject
import world.gregs.voidps.network.login.protocol.Decoder

class ObjectExamineDecoder : Decoder(2) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val objectId = packet.readUShort().toInt()
        return ExamineObject(objectId)
    }
}
