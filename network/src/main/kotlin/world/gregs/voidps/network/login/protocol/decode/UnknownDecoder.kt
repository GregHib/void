package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class UnknownDecoder : Decoder(2) {

    override suspend fun decode(packet: Source): Instruction? {
        val unknown = packet.readShort()
        return null
    }
}
