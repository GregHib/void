package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*

class InterfaceOnObjectDecoder : Decoder(15) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortAdd()
        val slot = packet.readShortAddLittle()
        val hash = packet.readIntLittleEndian()
        val type = packet.readShortAdd()
        val run = packet.readBooleanSubtract()
        val x = packet.readShortLittleEndian().toInt()
        val id = packet.readUnsignedShortLittle()
    }

}