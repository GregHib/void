package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedIntMiddle

class DialogueContinueDecoder(handler: Handler? = null) : Decoder(6, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.continueDialogue(
            player = player,
            button = packet.readShortAdd(),
            hash = packet.readUnsignedIntMiddle()
        )
    }

}