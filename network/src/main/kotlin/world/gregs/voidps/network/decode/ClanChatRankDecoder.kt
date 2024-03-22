package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ClanChatRank
import world.gregs.voidps.network.readByteSubtract
import world.gregs.voidps.network.readString

class ClanChatRankDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val rank = packet.readByteSubtract()
        val name = packet.readString()
        instructions.emit(ClanChatRank(name, rank))
    }

}