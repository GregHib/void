package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.ChangeDisplayMode

class ScreenChangeDecoder : Decoder(6) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        instructions.emit(ChangeDisplayMode(
            displayMode = packet.readUByte().toInt(),
            width = packet.readUShort().toInt(),
            height = packet.readUShort().toInt(),
            antialiasLevel = packet.readUByte().toInt()
        ))
    }

}