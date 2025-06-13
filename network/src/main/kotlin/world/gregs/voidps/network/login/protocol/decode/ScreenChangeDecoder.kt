package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChangeDisplayMode
import world.gregs.voidps.network.login.protocol.Decoder

class ScreenChangeDecoder : Decoder(6) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction = ChangeDisplayMode(
        displayMode = packet.readUByte().toInt(),
        width = packet.readUShort().toInt(),
        height = packet.readUShort().toInt(),
        antialiasLevel = packet.readUByte().toInt(),
    )
}
