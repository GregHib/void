package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class PingDecoder : Decoder(0) {

    override suspend fun decode(packet: Source): Instruction? = null
}
