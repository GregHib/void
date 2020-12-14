package rs.dusk.core.network.connection.client

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 18, 2020
 */
/*
fun main() {
	val factory = ConnectionFactory()
	
	val client =
		NetworkClient(ConnectionSettings("127.0.0.1", 43593))

    val chain = ChannelEventChain().apply {
        append(ChannelEventType.DEREGISTER, ReestablishmentEvent(client, limit = 10, delay = 1000))
    }

    val pipeline = ConnectionPipeline {
        it.addLast("message.handler", MessageReader(TestClientCodec()))
        it.addLast("connection.listener", ChannelEventListener(chain))
    }
	
    factory.connect(client, chain, pipeline)
}

private class TestClientCodec : Codec() {
    override fun register() {

    }

}*/
