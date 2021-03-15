package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ConsoleCommandDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        handler?.consoleCommand(
            player = player,
            packet.readString()
        )
    }

}