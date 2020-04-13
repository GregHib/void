package org.redrune.network.server

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.redrune.cache.cacheModule
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.SimplePacketDecoder
import org.redrune.core.network.connection.ConnectionPipeline
import org.redrune.core.network.connection.ConnectionSettings
import org.redrune.core.network.connection.server.NetworkServer
import org.redrune.engine.Startup
import org.redrune.engine.data.file.fileLoaderModule
import org.redrune.engine.data.file.ymlPlayerModule
import org.redrune.engine.entity.factory.entityFactoryModule
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.script.ScriptLoader
import org.redrune.network.NetworkRegistry
import org.redrune.network.rs.ServerNetworkEventHandler
import org.redrune.network.rs.codec.service.ServiceCodec
import org.redrune.network.rs.session.ServiceSession
import org.redrune.utility.getProperty
import org.redrune.World
import org.redrune.core.network.codec.message.encode.GenericMessageEncoder
import org.redrune.utility.func.PreloadableTask
import org.redrune.utility.get
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since April 13, 2020
 */
class GameServer(
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
            slf4jLogger()
            modules(eventBusModule, cacheModule, fileLoaderModule, ymlPlayerModule/*, sqlPlayerModule*/, entityFactoryModule)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
        ScriptLoader()
        NetworkRegistry().register()
    }

    override fun run() {
        preload()
        bind()

        val bus: EventBus = get()
        bus.emit(Startup())

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