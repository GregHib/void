package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.readByteInverse
import world.gregs.voidps.network.readUnsignedShortAddLittle

class PlayerOption1Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val index = packet.readUnsignedShortAddLittle()
        packet.readByteInverse()
        instructions.emit(InteractPlayer(index, 1))
    }

}