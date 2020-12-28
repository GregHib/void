package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.MovedCameraMessage

class MovedCameraMessageDecoder : GameMessageDecoder<MovedCameraMessage>(4) {

    override fun decode(packet: PacketReader) =
        MovedCameraMessage(packet.readUnsignedShort(), packet.readUnsignedShort())

}