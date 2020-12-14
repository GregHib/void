package rs.dusk.core.utility

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelPipeline

fun ChannelPipeline.replace(name : String, handler : ChannelHandler) : ChannelHandler? {
	return replace(name, name, handler)
}