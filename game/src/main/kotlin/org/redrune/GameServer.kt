package org.redrune

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.redrune.cache.Cache
import org.redrune.core.network.codec.Codec
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.RawMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.SimplePacketDecoder
import org.redrune.core.network.connection.ConnectionPipeline
import org.redrune.core.network.connection.ConnectionSettings
import org.redrune.core.network.connection.server.NetworkServer
import org.redrune.network.rs.codec.NetworkEventHandler
import org.redrune.network.rs.codec.game.GameCodec
import org.redrune.network.rs.codec.login.LoginCodec
import org.redrune.network.rs.codec.service.ServiceCodec
import org.redrune.network.rs.codec.update.UpdateCodec
import org.redrune.utility.YAMLParser
import org.redrune.utility.constants.GameConstants.Companion.BUILD_MAJOR
import org.redrune.utility.constants.GameConstants.Companion.BUILD_MINOR
import org.redrune.utility.constants.GameConstants.Companion.SERVER_NAME
import org.redrune.utility.constants.NetworkConstants
import org.redrune.world.World
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class GameServer(
    /**
     * The world this server represents
     */
    private val world: World
) {

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
        val settings = ConnectionSettings("localhost", NetworkConstants.PORT_ID + world.id)
        val server = NetworkServer(settings)
        val pipeline = ConnectionPipeline {
            it.addLast("packet.decoder", SimplePacketDecoder(ServiceCodec))
            it.addLast("message.decoder", OpcodeMessageDecoder(ServiceCodec))
            it.addLast("message.handler", NetworkMessageHandler(ServiceCodec, NetworkEventHandler()))
            it.addLast("message.encoder", RawMessageEncoder(ServiceCodec))
        }
        server.configure(pipeline)
        server.start()
    }

    /**
     * Tasks that need to be done before the server is loaded called here
     */
    fun preload() {
        YAMLParser.load()
        Cache.load()
        registerCodecs(GameCodec, ServiceCodec, LoginCodec, UpdateCodec)
    }

    private fun registerCodecs(vararg codecs: Codec) {
        for (codec in codecs) {
            codec.register()
            codec.report()
        }
    }


    fun start() {
        preload()
        bind()
        logger.info {
            "$SERVER_NAME v$BUILD_MAJOR.$BUILD_MINOR successfully booted world ${world.id} in ${stopwatch.elapsed(
                MILLISECONDS
            )} ms"
        }
        running = true
    }

}