package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class DialogueContinueDecoder : Decoder(6) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.continueDialogue(
            session = session,
            button = packet.readShortAdd(),
            hash = packet.readUnsignedIntMiddle()
        )
    }

}