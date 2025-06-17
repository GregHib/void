package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class MovedMouseDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? = null
}
