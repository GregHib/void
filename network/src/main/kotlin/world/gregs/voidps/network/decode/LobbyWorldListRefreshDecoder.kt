package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction

/**
 * Client has requested the lobby world list be refreshed
 */
class LobbyWorldListRefreshDecoder : Decoder(4) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val latency = packet.readInt()
    }

}