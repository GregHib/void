package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import kotlinx.io.readUByte
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class ConsoleCommandDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        packet.readUByte()
        packet.readUByte()
        val command = packet.readString()
        val parts = command.split(" ")
        val prefix = parts[0]
        return ExecuteCommand(prefix, command.removePrefix(prefix).trim())
    }
}
