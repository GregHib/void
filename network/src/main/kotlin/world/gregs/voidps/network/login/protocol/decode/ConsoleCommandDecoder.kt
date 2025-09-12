package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import kotlinx.io.readUByte
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class ConsoleCommandDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        val automatic = packet.readUByte().toInt() == 1
        val retainText = packet.readUByte().toInt() == 1
        val command = packet.readString()
        return ExecuteCommand(command, automatic, retainText)
    }
}
