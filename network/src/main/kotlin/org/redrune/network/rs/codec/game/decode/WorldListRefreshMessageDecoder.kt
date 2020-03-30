package org.redrune.network.rs.codec.game.decode

import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.network.rs.codec.game.GameMessageDecoder
import org.redrune.network.rs.codec.game.decode.message.WorldListRefreshMessage
import org.redrune.utility.constants.game.GameOpcodes.REFRESH_WORLDS

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