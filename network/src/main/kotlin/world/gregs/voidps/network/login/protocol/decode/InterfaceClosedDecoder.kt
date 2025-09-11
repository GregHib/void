package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InterfaceClosedInstruction
import world.gregs.voidps.network.login.protocol.Decoder

class InterfaceClosedDecoder : Decoder(0) {

    override suspend fun decode(packet: Source): Instruction = InterfaceClosedInstruction
}
