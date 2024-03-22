package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readString

class AddIgnoreDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(world.gregs.voidps.network.client.instruction.IgnoreAdd(packet.readString()))
    }

}