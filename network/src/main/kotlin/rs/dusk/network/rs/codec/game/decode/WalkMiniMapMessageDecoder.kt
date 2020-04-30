package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.MINI_MAP_WALK
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage

@PacketMetaData(opcodes = [MINI_MAP_WALK], length = 18)
class WalkMiniMapMessageDecoder : GameMessageDecoder<WalkMiniMapMessage>() {

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