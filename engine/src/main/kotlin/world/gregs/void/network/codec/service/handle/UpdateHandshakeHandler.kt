package world.gregs.void.network.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Handler
import world.gregs.void.network.codec.service.FileServerResponseCodes
import world.gregs.void.network.codec.setCodec
import world.gregs.void.network.codec.update.UpdateCodec
import world.gregs.void.network.codec.update.encode.UpdateVersionEncoder
import world.gregs.void.utility.getIntProperty
import world.gregs.void.utility.inject

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