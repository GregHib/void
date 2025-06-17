package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class MovedCameraDecoder : Decoder(4) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val pitch = packet.readUShort().toInt()
        val yaw = packet.readUShort().toInt()
        return null
    }
}
