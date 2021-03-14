package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceOnObjectDecoder : Decoder(15) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceOnObject(
            player = player,
            y = packet.readShortAdd(),
            slot = packet.readShortAddLittle(),
            hash = packet.readIntLittle(),
            type = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            x = packet.readShortLittle(),
            id = packet.readUnsignedShortLittle()
        )
    }

}