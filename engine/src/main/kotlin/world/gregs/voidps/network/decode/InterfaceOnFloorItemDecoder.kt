package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val x = packet.readShort().toInt()
        val y = packet.readShort().toInt()
        val floorType = packet.readShortAddLittle()
        val hash = packet.readIntInverseMiddle()
        val slot = packet.readShortLittleEndian().toInt()
        val run = packet.readBoolean()
        val item = packet.readShortLittleEndian().toInt()
    }

}