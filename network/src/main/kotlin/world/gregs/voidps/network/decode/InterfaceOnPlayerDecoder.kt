package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*

class InterfaceOnPlayerDecoder : Decoder(1) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val playerIndex = packet.readShortAddLittle()
        val type = packet.readShortLittleEndian().toInt()
        val slot = packet.readShortLittleEndian().toInt()
        val hash = packet.readIntInverseMiddle()
        val run = packet.readBooleanInverse()
    }

}