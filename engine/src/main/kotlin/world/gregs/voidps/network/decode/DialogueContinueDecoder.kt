package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class DialogueContinueDecoder : Decoder(6) {

    override fun decode(player: Player, packet: Reader) {
        handler?.continueDialogue(
            player = player,
            button = packet.readShortAdd(),
            hash = packet.readUnsignedIntMiddle()
        )
    }

}