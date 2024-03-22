package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.readString

class HyperlinkDecoder : Decoder(BYTE) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val name = packet.readString()
        val script = packet.readString()
        val third = packet.readByte()
    }

}