package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.ChatTypeChange

/**
 * Notified the type of message before a message is sent
 * The type of message sent (0 = public, 1 = friends chat)
 */
class ChatTypeDecoder : Decoder(1) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(ChatTypeChange(packet.readUByte().toInt()))
    }

}