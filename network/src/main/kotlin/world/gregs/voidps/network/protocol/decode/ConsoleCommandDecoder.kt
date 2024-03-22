package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readString

class ConsoleCommandDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        packet.readUByte()
        packet.readUByte()
        val command = packet.readString()
        val parts = command.split(" ")
        val prefix = parts[0]
        instructions.emit(ExecuteCommand(prefix, command.removePrefix(prefix).trim()))
    }

}