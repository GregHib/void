package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ReflectionResponseDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readByte()//0
        handler?.reflectionResponse(player)
    }
}