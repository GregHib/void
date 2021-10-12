package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.readIntInverseMiddle
import world.gregs.voidps.network.readShortAdd

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromHash = packet.readInt()
        val toHash = packet.readIntInverseMiddle()
        val fromItem = packet.readShortAdd()
        val from = packet.readShort().toInt()
        val toItem = packet.readShortAdd()
        val to = packet.readShort().toInt()
    }

}