package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAdd

class APCoordinateDecoder : Decoder(12) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val x = packet.readShortLittleEndian()
        val first = packet.readShortAdd()
        val third = packet.readShortLittleEndian()
        val fourth = packet.readInt()
        val y = packet.readShortAdd()
    }

}