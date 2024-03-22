package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.EnterString
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.readString

class StringEntryDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(EnterString(packet.readString()))
    }

}