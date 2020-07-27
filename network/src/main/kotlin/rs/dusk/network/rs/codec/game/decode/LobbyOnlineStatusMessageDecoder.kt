package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ONLINE_STATUS
import rs.dusk.network.rs.codec.game.decode.message.LobbyOnlineStatusMessage

@PacketMetaData(opcodes = [ONLINE_STATUS], length = 3)
class LobbyOnlineStatusMessageDecoder : GameMessageDecoder<LobbyOnlineStatusMessage>() {

    override fun decode(packet: PacketReader) =
        LobbyOnlineStatusMessage(packet.readByte(), packet.readByte(), packet.readByte())

}