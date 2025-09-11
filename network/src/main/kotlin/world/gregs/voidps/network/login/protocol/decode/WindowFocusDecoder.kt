package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean

class WindowFocusDecoder : Decoder(1) {

    override suspend fun decode(packet: Source): Instruction? {
        val focused = packet.readBoolean()
        return null
    }
}
