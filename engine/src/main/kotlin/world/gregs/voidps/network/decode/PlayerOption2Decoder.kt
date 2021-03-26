package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractPlayer

class PlayerOption2Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        packet.readByte()
        val index = packet.readShort().toInt()
        instructions.emit(InteractPlayer(index, 2))
    }

}