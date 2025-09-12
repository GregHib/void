package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ClanChatJoin
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class ClanChatJoinDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction = ClanChatJoin(packet.readString())
}
