package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readUByte
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChatTypeChange
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * Notified the type of message before a message is sent
 * The type of message sent (0 = public, 1 = clan chat)
 */
class ChatTypeDecoder : Decoder(1) {
    override suspend fun decode(packet: Source): Instruction = ChatTypeChange(packet.readUByte().toInt())
}
