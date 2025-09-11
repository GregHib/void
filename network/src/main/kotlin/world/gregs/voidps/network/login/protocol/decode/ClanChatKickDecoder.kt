package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ClanChatKick
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class ClanChatKickDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction = ClanChatKick(packet.readString())
}
