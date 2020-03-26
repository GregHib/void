package org.redrune.network.codec.game.decode.impl

import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.core.network.model.packet.access.PacketReader
import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.game.decode.message.WorldListRefreshMessage
import org.redrune.tools.constants.GameOpcodes.REFRESH_WORLDS

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