package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ReflectionResponseDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        packet.readByte()//0
        handler?.reflectionResponse(player)
    }
}