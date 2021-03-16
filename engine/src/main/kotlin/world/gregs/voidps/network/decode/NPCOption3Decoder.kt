package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBoolean

class NPCOption3Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.npcOption(
            player = player,
            npcIndex = packet.readShort().toInt(),
            run = packet.readBoolean(),
            option = 3
        )
    }

}