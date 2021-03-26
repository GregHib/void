package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.readBoolean
import world.gregs.voidps.network.readString

class ClanChatKickDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val owner = packet.readBoolean()
        val equals = packet.readShort().toInt()
        val member = packet.readString()
    }

}