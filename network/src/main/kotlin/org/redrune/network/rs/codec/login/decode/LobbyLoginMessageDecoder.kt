package org.redrune.network.rs.codec.login.decode

import org.redrune.cache.CacheDelegate
import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.core.network.model.packet.PacketType
import org.redrune.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import org.redrune.network.rs.codec.login.LoginMessageDecoder
import org.redrune.network.rs.codec.login.decode.message.LobbyLoginMessage
import org.redrune.utility.constants.network.ServiceOpcodes
import org.redrune.utility.constants.network.ServiceOpcodes.LOBBY_LOGIN
import org.redrune.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [LOBBY_LOGIN], length = VARIABLE_LENGTH_SHORT)
class LobbyLoginMessageDecoder : LoginMessageDecoder<LobbyLoginMessage>() {

    private val cache by inject<CacheDelegate>()

    override fun decode(packet: PacketReader): LobbyLoginMessage {
        val triple = LoginHeaderDecoder.decode(packet)
        val password = triple.second!!
        val isaacKeys = triple.third!!

        val username = packet.readString()
        val highDefinition = packet.readBoolean()
        val resizeable = packet.readBoolean()
        packet.skip(24)
        val settings = packet.readString()
        val affiliate = packet.readInt()
        val crcMap = mutableMapOf<Int, Pair<Int, Int>>()

        for (index in 0..35) {
            val indexCrc = cache.getIndexCrc(index)
            val clientCrc = packet.readInt()
            crcMap[index] = Pair(indexCrc, clientCrc)
        }
        return LobbyLoginMessage(username, password, highDefinition, resizeable, settings, affiliate, isaacKeys, crcMap)
    }
}