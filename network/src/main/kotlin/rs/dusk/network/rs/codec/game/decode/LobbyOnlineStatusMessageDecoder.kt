package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.LobbyOnlineStatusMessage

class LobbyOnlineStatusMessageDecoder : GameMessageDecoder<LobbyOnlineStatusMessage>(3) {

    override fun decode(packet: PacketReader) =
        LobbyOnlineStatusMessage(packet.readByte(), packet.readByte(), packet.readByte())

}