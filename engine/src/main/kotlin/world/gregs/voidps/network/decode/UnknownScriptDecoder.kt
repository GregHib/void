package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class UnknownScriptDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.unknownScript(
            player = player,
            string = packet.readString()
        )
    }

}