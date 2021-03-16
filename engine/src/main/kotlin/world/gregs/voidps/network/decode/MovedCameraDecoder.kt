package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class MovedCameraDecoder : Decoder(4) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.cameraMoved(
            player = player,
            pitch = packet.readUShort().toInt(),
            yaw = packet.readUShort().toInt()
        )
    }

}