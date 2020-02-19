package org.redrune.network.codec.login.decode.impl

import org.redrune.cache.Cache
import org.redrune.network.codec.login.decode.LoginHeader
import org.redrune.network.codec.login.decode.LoginServiceMessageDecoder
import org.redrune.network.codec.login.decode.message.LoginServiceLobbyMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.PacketType
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.ServiceOpcodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [ServiceOpcodes.LOBBY_LOGIN], length = PacketType.VARIABLE_LENGTH_SHORT)
class LoginServiceLobbyMessageDecoder : LoginServiceMessageDecoder<LoginServiceLobbyMessage>() {
    override fun decode(packet: PacketReader): LoginServiceLobbyMessage {
        val triple = LoginHeader.decode(packet)
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
            val indexCrc = Cache.indices[index].crc
            val clientCrc = packet.readInt()
            crcMap.put(index, Pair(indexCrc, clientCrc))
        }
        return LoginServiceLobbyMessage(username, password, highDefinition, resizeable, settings, affiliate, isaacKeys, crcMap)
    }
}