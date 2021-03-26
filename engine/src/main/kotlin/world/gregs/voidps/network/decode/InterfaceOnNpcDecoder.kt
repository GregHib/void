package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*

class InterfaceOnNpcDecoder : Decoder(11) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val slot = packet.readShortAddLittle()
        val hash = packet.readInt()
        val type = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        val npc = packet.readShortAdd()
    }

}