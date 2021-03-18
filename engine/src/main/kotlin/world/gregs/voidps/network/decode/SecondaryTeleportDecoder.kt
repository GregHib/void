package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readShortAddLittle

class SecondaryTeleportDecoder(handler: Handler? = null) : Decoder(4, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.secondaryTeleport(
            player = player,
            x = packet.readShortAddLittle(),
            y = packet.readShortLittleEndian().toInt()
        )
    }

}