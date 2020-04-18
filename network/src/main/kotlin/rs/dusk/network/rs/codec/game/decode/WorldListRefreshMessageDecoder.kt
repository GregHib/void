package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REFRESH_WORLDS
import rs.dusk.network.rs.codec.game.decode.message.WorldListRefreshMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 19, 2020
 */
@PacketMetaData(opcodes = [REFRESH_WORLDS], length = 4)
class WorldListRefreshMessageDecoder : GameMessageDecoder<WorldListRefreshMessage>() {

    override fun decode(packet: PacketReader): WorldListRefreshMessage {
        val crc = packet.readInt()
        return WorldListRefreshMessage(crc)
    }


}