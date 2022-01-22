package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.IgnoreDelete
import world.gregs.voidps.network.readString

class DeleteIgnoreDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(IgnoreDelete(packet.readString()))
    }

}