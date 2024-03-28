package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InterfaceClosedInstruction
import world.gregs.voidps.network.login.protocol.Decoder

class InterfaceClosedDecoder : Decoder(0) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(InterfaceClosedInstruction)
    }

}