package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        packet.readInt() // 1057001181
        return null
    }
}
