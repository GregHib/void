package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAdd

class APCoordinateDecoder : Decoder(12) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val x = packet.readShortLittleEndian()
        val first = packet.readShortAdd()
        val third = packet.readShortLittleEndian()
        val fourth = packet.readInt()
        val y = packet.readShortAdd()
        return null
    }
}
