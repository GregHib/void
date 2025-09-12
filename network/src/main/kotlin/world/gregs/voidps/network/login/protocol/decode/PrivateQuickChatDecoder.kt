package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.QuickChatPrivate
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class PrivateQuickChatDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        val name = packet.readString()
        val file = packet.readUShort().toInt()
        val data = packet.readByteArray(packet.remaining.toInt())
        return QuickChatPrivate(name, file, data)
    }

}
