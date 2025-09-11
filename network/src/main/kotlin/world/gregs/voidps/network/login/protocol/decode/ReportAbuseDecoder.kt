package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ReportAbuse
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class ReportAbuseDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        val name = packet.readString()
        val type = packet.readByte().toInt()
        val integer = packet.readByte().toInt()
        val string = packet.readString()
        return ReportAbuse(name, type, integer, string)
    }
}
