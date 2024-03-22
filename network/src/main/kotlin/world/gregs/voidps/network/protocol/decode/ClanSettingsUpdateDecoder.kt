package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readString

class ClanSettingsUpdateDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val first = packet.readShort().toInt()
        val string = packet.readString()
    }

}