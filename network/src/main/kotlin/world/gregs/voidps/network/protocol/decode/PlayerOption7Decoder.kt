package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readByteAdd
import world.gregs.voidps.network.protocol.readShortAdd

class PlayerOption7Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val index = packet.readShortAdd()
        packet.readByteAdd()
        instructions.emit(InteractPlayer(index, 7))
    }

}