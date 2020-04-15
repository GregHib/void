package rs.dusk.network.server

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.koin.core.context.startKoin
import rs.dusk.World
import rs.dusk.cache.cacheModule
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.message.handle.NetworkMessageHandler
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.connection.ConnectionPipeline
import rs.dusk.core.network.connection.ConnectionSettings
import rs.dusk.core.network.connection.server.NetworkServer
import rs.dusk.network.rs.ServerNetworkEventHandler
import rs.dusk.network.rs.codec.service.ServiceCodec
import rs.dusk.network.rs.session.ServiceSession
import rs.dusk.utility.func.PreloadableTask
import rs.dusk.utility.getProperty
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since April 13, 2020
 */
class LobbyServer(
    /**
     * The world this server represents
     */
    private val world: World
) : PreloadableTask {

    private val logger = InlineLogger()

    /**
     * The stopwatch instance
     */
    private val stopwatch = Stopwatch.createStarted()

    /**
     * If the game server is running
     */
    var running = false

    private fun bind() {
        val port = getProperty<Int>("port")!!
        val settings = ConnectionSettings("localhost", port + world.id)
        val server = NetworkServer(settings)
        val pipeline = ConnectionPipeline {
            it.addLast("packet.decoder", SimplePacketDecoder(ServiceCodec))
            it.addLast("message.decoder", OpcodeMessageDecoder(ServiceCodec))
            it.addLast(
                "message.handler", NetworkMessageHandler(
                    ServiceCodec,
                    ServerNetworkEventHandler(ServiceSession(it.channel()))
                )
            )
            it.addLast("message.encoder", GenericMessageEncoder(ServiceCodec))
        }
        server.configure(pipeline)
        server.start()
    }

    override fun preload() {
        startKoin {
            modules(cacheModule)
            fileProperties("/game.properties")
            fileProperties("/rsa.properties")
        }
    }

    override fun run() {
        preload()
        bind()

        logger.info {
            val name = getProperty<String>("name")
            val major = getProperty<Int>("buildMajor")
            val minor = getProperty<Float>("buildMinor")

            "$name v$major.$minor successfully booted world ${world.id} in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)} ms"
        }
        running = true
    }

}

fun main() {
    val world = World(1)
    val server = GameServer(world)

    server.run()
}