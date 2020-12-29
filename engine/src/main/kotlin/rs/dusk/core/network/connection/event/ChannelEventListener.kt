package rs.dusk.core.network.connection.event

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import rs.dusk.core.network.connection.event.ChannelEventType.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
class ChannelEventListener(private val chain : ChannelEventChain) : ChannelInboundHandlerAdapter() {
	
	override fun channelRegistered(ctx : ChannelHandlerContext) {
		chain.handle(REGISTER, ctx)
	}
	
	override fun channelUnregistered(ctx : ChannelHandlerContext) {
		chain.handle(DEREGISTER, ctx)
	}
	
	override fun channelActive(ctx : ChannelHandlerContext) {
		chain.handle(ACTIVE, ctx)
	}
	
	override fun channelInactive(ctx : ChannelHandlerContext) {
		chain.handle(INACTIVE, ctx)
	}
	
	override fun exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
		chain.handle(EXCEPTION, ctx, cause)
	}
	
	
}