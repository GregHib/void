package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class NPCOption4Decoder : Decoder(3) {

    override fun decode(player: Player, packet: Reader) {
        handler?.npcOption(
            player = player,
            npcIndex = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            option = 4
        )
    }

}