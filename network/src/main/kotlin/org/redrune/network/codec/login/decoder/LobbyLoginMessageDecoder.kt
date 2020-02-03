package org.redrune.network.codec.login.decoder

import com.github.michaelbull.logging.InlineLogger
import org.redrune.cache.Cache
import org.redrune.network.codec.login.LoginHeader
import org.redrune.network.codec.login.LoginOpcodes
import org.redrune.network.codec.login.message.LobbyLoginMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader
import org.redrune.network.model.packet.PacketType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LobbyLoginMessageDecoder : MessageDecoder(PacketType.SHORT, LoginOpcodes.LOBBY_LOGIN) {

    private val logger = InlineLogger()

    override fun decode(reader: PacketReader): Message {
        val triple = LoginHeader.decode(reader)
        val password = triple.second!!
        val isaacKeys = triple.third!!

        val username = reader.readString()
        val highDefinition = reader.readBoolean()
        val resizeable = reader.readBoolean()
        reader.skip(24)
        val settings = reader.readString()
        val affiliate = reader.readInt()
        val crcMap = mutableMapOf<Int, Pair<Int, Int>>()

        for (index in 0..37) {
            val indexCrc = Cache.indices[index].crc
            val clientCrc = reader.readInt()
            crcMap.put(index, Pair(indexCrc, clientCrc))
        }
        return LobbyLoginMessage(username, password, highDefinition, resizeable, settings, affiliate, isaacKeys, crcMap)
    }

}