package rs.dusk.network.rs.codec.login.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.Cache
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.SHORT
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginMessageDecoder : MessageDecoder(SHORT) {

    private val cache: Cache by inject()

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
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
        handler?.loginLobby(
            context = context,
            username = username,
            password = password,
            hd = highDefinition,
            resize = resizeable,
            settings = settings,
            affiliate = affiliate,
            isaacSeed = isaacKeys,
            crcMap = crcMap
        )
    }
}