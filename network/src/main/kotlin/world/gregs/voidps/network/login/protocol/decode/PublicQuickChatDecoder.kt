package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.QuickChatPublic
import world.gregs.voidps.network.login.protocol.Decoder

class PublicQuickChatDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        val script = packet.readByte().toInt()
        val file = packet.readUShort().toInt()
        val data = packet.readByteArray(packet.remaining.toInt())
        return QuickChatPublic(script, file, data)
    }
}
