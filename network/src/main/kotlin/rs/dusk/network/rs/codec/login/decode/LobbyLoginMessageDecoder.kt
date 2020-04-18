package rs.dusk.network.rs.codec.login.decode

import rs.dusk.cache.Cache
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import rs.dusk.network.rs.codec.login.LoginMessageDecoder
import rs.dusk.network.rs.codec.login.decode.message.LobbyLoginMessage
import rs.dusk.network.rs.codec.service.ServiceOpcodes.LOBBY_LOGIN
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [LOBBY_LOGIN], length = VARIABLE_LENGTH_SHORT)
class LobbyLoginMessageDecoder : LoginMessageDecoder<LobbyLoginMessage>() {

    private val cache: Cache by inject()

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