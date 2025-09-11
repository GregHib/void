package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChangeDisplayMode
import world.gregs.voidps.network.login.protocol.Decoder

class ScreenChangeDecoder : Decoder(6) {

    override suspend fun decode(packet: Source): Instruction = ChangeDisplayMode(
        displayMode = packet.readUByte().toInt(),
        width = packet.readUShort().toInt(),
        height = packet.readUShort().toInt(),
        antialiasLevel = packet.readUByte().toInt(),
    )

}
