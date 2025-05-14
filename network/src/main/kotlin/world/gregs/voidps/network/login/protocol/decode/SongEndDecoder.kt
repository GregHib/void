package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.SongEnd
import world.gregs.voidps.network.login.protocol.Decoder

class SongEndDecoder : Decoder(4) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val songIndex = packet.readInt()
        return SongEnd(
            songIndex
        )
    }

}