package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class MovedCameraDecoder : Decoder(4) {

    override suspend fun decode(packet: Source): Instruction? {
        val pitch = packet.readUShort().toInt()
        val yaw = packet.readUShort().toInt()
        return null
    }

}
