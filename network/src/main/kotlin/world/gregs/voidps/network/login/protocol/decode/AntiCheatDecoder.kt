package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class AntiCheatDecoder : Decoder(2) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val packetsReceived = packet.readShort()
        return null
    }
}
