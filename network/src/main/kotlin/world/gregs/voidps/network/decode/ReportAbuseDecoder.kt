package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ReportAbuse
import world.gregs.voidps.network.readString

class ReportAbuseDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val name = packet.readString()
        val type = packet.readByte().toInt()
        val integer = packet.readByte().toInt()
        val string = packet.readString()
        instructions.emit(ReportAbuse(name, type, integer, string))
    }

}