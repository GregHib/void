package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.QuickChatPrivate
import world.gregs.voidps.network.readString

class PrivateQuickChatDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val name = packet.readString()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
        instructions.emit(QuickChatPrivate(name, file, data))
    }

}