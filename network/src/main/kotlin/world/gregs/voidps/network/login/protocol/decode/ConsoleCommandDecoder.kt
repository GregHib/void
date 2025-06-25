package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readGjstr

class ConsoleCommandDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val length = packet.readUByte()
        val a = packet.readUByte()
        val command = packet.readGjstr()
        val parts = command.split(" ")
        val prefix = parts[0]
        return ExecuteCommand(prefix, command.removePrefix(prefix).trim())
    }

}