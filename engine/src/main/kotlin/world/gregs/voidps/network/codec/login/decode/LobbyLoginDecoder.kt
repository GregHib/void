package world.gregs.voidps.network.codec.login.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.SHORT
import world.gregs.voidps.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginDecoder : Decoder(SHORT) {

    private val cache: Cache by inject()

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
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

        for (index in 0..34) {
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