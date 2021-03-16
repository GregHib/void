package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBoolean

class NPCOption1Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.npcOption(
            player = player,
            run = packet.readBoolean(),
            npcIndex = packet.readShortLittleEndian().toInt(),
            option = 1
        )
    }

}