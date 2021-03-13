package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnNpcDecoder : Decoder(11) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceOnNPC(
            player = player,
            slot = packet.readShortAddLittle(),
            hash = packet.readInt(),
            type = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            npc = packet.readShortAdd()
        )
    }

}