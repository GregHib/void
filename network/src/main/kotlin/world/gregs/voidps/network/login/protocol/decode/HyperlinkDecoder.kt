package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class HyperlinkDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction? {
        val name = packet.readString()
        val script = packet.readString()
        val third = packet.readByte()
        return null
    }
}
