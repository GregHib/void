package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.MOVE_CAMERA
import rs.dusk.network.rs.codec.game.decode.message.MovedCameraMessage

@PacketMetaData(opcodes = [MOVE_CAMERA], length = 4)
class MovedCameraMessageDecoder : GameMessageDecoder<MovedCameraMessage>() {

    override fun decode(packet: PacketReader) =
        MovedCameraMessage(packet.readUnsignedShort(), packet.readUnsignedShort())

}