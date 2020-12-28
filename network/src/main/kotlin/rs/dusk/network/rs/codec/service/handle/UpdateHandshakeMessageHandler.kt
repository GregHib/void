package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.service.FileServerResponseCodes
import rs.dusk.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.rs.codec.update.encode.message.UpdateVersionMessage
import rs.dusk.utility.getIntProperty

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateHandshakeMessageHandler : MessageHandler<UpdateHandshakeMessage>() {
	
	private val clientMajorBuild: Int = getIntProperty("clientBuild")
	
	override fun handle(ctx : ChannelHandlerContext, msg : UpdateHandshakeMessage) {
		val major = msg.major
		val response =
			if (major == clientMajorBuild) {
				FileServerResponseCodes.JS5_RESPONSE_OK
			} else {
				FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
			}
		
		val channel = ctx.channel()
		val pipeline = ctx.pipeline()
		
		pipeline.apply {
			replace("packet.decoder", SimplePacketDecoder())
			replace("message.decoder", OpcodeMessageDecoder())
			replace("message.reader", MessageReader())
			replace("message.encoder", GenericMessageEncoder())
			
			channel.setCodec(UpdateCodec)
		}
		pipeline.writeAndFlush(UpdateVersionMessage(response))
	}
}