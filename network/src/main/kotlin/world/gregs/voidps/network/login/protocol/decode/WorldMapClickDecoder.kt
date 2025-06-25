package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.instruction.WorldMapClick
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.g4Alt3

class WorldMapClickDecoder : Decoder(4) {

    override suspend fun decode(packet: ByteReadPacket) = WorldMapClick(packet.g4Alt3())

}