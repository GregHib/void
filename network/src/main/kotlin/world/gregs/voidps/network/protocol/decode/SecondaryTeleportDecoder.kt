package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.readShortAddLittle

class SecondaryTeleportDecoder : Decoder(4) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val x = packet.readShortAddLittle()
        val y = packet.readShortLittleEndian().toInt()
    }

}