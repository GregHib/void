package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * Client has requested the lobby world list be refreshed
 */
class LobbyWorldListRefreshDecoder : Decoder(4) {

    override suspend fun decode(packet: Source): Instruction? {
        val latency = packet.readInt()
        return null
    }
}
