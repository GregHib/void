package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractPlayer
import world.gregs.voidps.network.readByteInverse

class PlayerOption1Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val index = packet.readShortLittleEndian().toInt()
        packet.readByteInverse()
        instructions.emit(InteractPlayer(index, 1))
    }

}