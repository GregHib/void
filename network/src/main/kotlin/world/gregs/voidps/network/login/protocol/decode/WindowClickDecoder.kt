package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class WindowClickDecoder : Decoder(6) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val packed = packet.readShort().toInt()
        val position = packet.readInt()
        return null
    }
}
