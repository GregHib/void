package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.EnterName
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class NameEntryDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction = EnterName(packet.readString())
}
