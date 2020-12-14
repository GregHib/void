package rs.dusk.core.network.connection.server

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 18, 2020
 */
class NetworkServerTest

/*
fun main() {
	val factory = ServerConnectionFactory()
	
	val settings = ConnectionSettings("localhost", 43593)
	val server = NetworkServer(settings)
	
	*/
/*
	val config = SslConfig("./", "", "") ?: throw IllegalStateException("Unable to create ssl configuration")
	val sslInitializer = SslServerInitializer(config) // TODO
	*//*

	
	val chain = ChannelEventChain()
	
	val pipeline = ConnectionPipeline {
		it.addLast("message.handler", MessageReader())
		it.addLast("connection.listener", ChannelEventListener(chain))
	}
	factory.bind(server, chain, pipeline)
}*/
