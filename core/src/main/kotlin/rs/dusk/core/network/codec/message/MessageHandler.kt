package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.message.Message

abstract class MessageHandler<M : Message> {
	
	/**
	 * Handles what to do with message [M]
	 */
	abstract fun handle(ctx : ChannelHandlerContext, msg : M)
}