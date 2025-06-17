package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class ReflectionResponseDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        packet.readByte()
        return null
    }
}
