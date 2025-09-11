package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.IgnoreAdd
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class AddIgnoreDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction = IgnoreAdd(packet.readString())
}
