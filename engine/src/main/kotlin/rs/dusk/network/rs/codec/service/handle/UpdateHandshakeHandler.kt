package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Handler
import rs.dusk.core.network.codec.setCodec
import rs.dusk.network.rs.codec.service.FileServerResponseCodes
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.rs.codec.update.encode.UpdateVersionEncoder
import rs.dusk.utility.getIntProperty
import rs.dusk.utility.inject

class UpdateHandshakeHandler : Handler() {

    private val clientMajorBuild: Int = getIntProperty("clientBuild")
    private val update: UpdateCodec by inject()
    private val versionEncoder = UpdateVersionEncoder()

    override fun updateHandshake(context: ChannelHandlerContext, version: Int) {
        val response = if (version == clientMajorBuild) {
            FileServerResponseCodes.JS5_RESPONSE_OK
        } else {
            FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
        }

        val channel = context.channel()
        channel.setCodec(update)
        versionEncoder.encode(channel, response)
    }
}