package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineObject
import world.gregs.voidps.network.login.protocol.Decoder

class ObjectExamineDecoder : Decoder(2) {

    override suspend fun decode(packet: Source): Instruction {
        val objectId = packet.readUShort().toInt()
        return ExamineObject(objectId)
    }

}
