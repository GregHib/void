package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean

class WindowFocusDecoder : Decoder(1) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val focused = packet.readBoolean()
        return null
    }
}
