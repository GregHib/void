package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyLoginConnectionResponseMessage
import rs.dusk.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameConnectionHandshakeMessageHandler : MessageHandler<GameConnectionHandshakeMessage>() {
	
	override fun handle(ctx : ChannelHandlerContext, msg : GameConnectionHandshakeMessage) {
		val pipeline = ctx.pipeline()
		val channel = ctx.channel()
		
		channel.setCodec(LoginCodec)
		
		pipeline.apply {
			replace("packet.decoder", SimplePacketDecoder())
			replace("message.decoder", OpcodeMessageDecoder())
			replace("message.reader", MessageReader())
			replace("message.encoder", GenericMessageEncoder())
		}
		ctx.pipeline().writeAndFlush(LobbyLoginConnectionResponseMessage(0))
	}
	
}