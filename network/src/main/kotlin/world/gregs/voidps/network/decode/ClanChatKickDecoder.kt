package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ClanChatKick
import world.gregs.voidps.network.readString

class ClanChatKickDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(ClanChatKick(packet.readString()))
    }

}