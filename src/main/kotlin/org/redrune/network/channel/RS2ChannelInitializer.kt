package org.redrune.network.channel

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.codec.handshake.HandshakeDecoder
import org.redrune.network.codec.packet.RS2PacketEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
class RS2ChannelInitializer : ChannelInitializer<SocketChannel>() {

    /**
     * The instance of the channel reader
     */
    private val channelReader = RS2ChannelReader()

    /**
     * The instance of the channel registrar
     */
    private val channelRegistrar = RS2ChannelRegistrar()

    override fun initChannel(ch: SocketChannel) {
        val pipeline: ChannelPipeline = ch.pipeline()
        pipeline.addLast("encoder", RS2PacketEncoder())
        pipeline.addLast("decoder", HandshakeDecoder())
        pipeline.addLast("handler", channelReader)
        pipeline.addLast("registrar", channelRegistrar)
        pipeline.channel().attr(NetworkConstants.SESSION_KEY).set(NetworkSession(ch))
    }
}