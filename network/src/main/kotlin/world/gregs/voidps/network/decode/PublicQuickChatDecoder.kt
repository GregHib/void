package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction

class PublicQuickChatDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val script = packet.readByte().toInt()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
    }

}