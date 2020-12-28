package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage

class WalkMiniMapMessageDecoder : GameMessageDecoder<WalkMiniMapMessage>(18) {

    override fun decode(packet: PacketReader): WalkMiniMapMessage {
        val baseX = packet.readShort(Modifier.ADD, Endian.LITTLE)
        val baseY = packet.readShort(Modifier.ADD, Endian.LITTLE)
        val running = packet.readBoolean()
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
        return WalkMiniMapMessage(baseX, baseY, running)
    }

}