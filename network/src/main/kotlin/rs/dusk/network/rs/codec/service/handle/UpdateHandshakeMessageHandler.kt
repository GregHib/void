package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import org.koin.java.KoinJavaComponent
import rs.dusk.core.network.codec.CodecRepository
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.connection.event.ConnectionEventListener
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.ServerConnectionEventChain
import rs.dusk.network.rs.codec.service.FileServerResponseCodes
import rs.dusk.network.rs.codec.service.ServiceMessageHandler
import rs.dusk.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.rs.codec.update.encode.message.UpdateVersionMessage
import rs.dusk.network.rs.session.UpdateSession
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateHandshakeMessageHandler : ServiceMessageHandler<UpdateHandshakeMessage>() {

	private val clientMajorBuild = getProperty<Int>("clientBuild")
	
	private val repository: CodecRepository by inject()

	override fun handle(ctx: ChannelHandlerContext, msg: UpdateHandshakeMessage) {
		val major = msg.major
		val response =
			if (major == clientMajorBuild) {
				FileServerResponseCodes.JS5_RESPONSE_OK
			} else {
				FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
			}

		val codec = repository.get(UpdateCodec::class)
		val pipeline = ctx.pipeline()
		pipeline.apply {
			val session = UpdateSession(channel())

			replace("packet.decoder", SimplePacketDecoder(codec))
			replace("message.decoder", OpcodeMessageDecoder(codec))
			replace(
				"message.reader", MessageReader(
					codec
				)
			)
			replace("message.encoder", GenericMessageEncoder(codec))
			replace("connection.listener", ConnectionEventListener(ServerConnectionEventChain(session)))
		}
		pipeline.writeAndFlush(UpdateVersionMessage(response))
	}
}