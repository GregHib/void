package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractPlayer
import world.gregs.voidps.network.readByteAdd
import world.gregs.voidps.network.readShortAdd

class PlayerOption7Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val index = packet.readShortAdd()
        packet.readByteAdd()
        instructions.emit(InteractPlayer(index, 7))
    }

}