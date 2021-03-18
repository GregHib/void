package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class WalkMiniMapDecoder(handler: Handler? = null) : Decoder(18, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.minimapWalk(
            player = player,
            y = packet.readShortLittleEndian().toInt(),
            running = packet.readBooleanAdd(),
            x = packet.readUnsignedShortAdd()
        )
        packet.readByte()//-1
        packet.readByte()//-1
        packet.readShort()//Rotation?
        packet.readByte()//57
        val minimapRotation = packet.readByte()
        val minimapZoom = packet.readByte()
        packet.readByte()//89
        packet.readShort()//X in region?
        packet.readShort()//Y in region?
        packet.readByte()//63
    }

}