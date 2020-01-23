package org.redrune

import com.google.common.base.Stopwatch
import org.redrune.cache.Cache
import org.redrune.engine.GameCycleWorker
import org.redrune.network.NetworkInitializer
import org.redrune.network.codec.CodecRegistry
import org.redrune.tools.YAMLParser
import org.redrune.tools.constants.GameConstants
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object GameServer {

    /**
     * The logger for this class
     */
    private val logger = LoggerFactory.getLogger(GameServer::class.java)

    /**
     * The instance of the network
     */
    private val network = NetworkInitializer()

    /**
     * If the game server is running
     */
    var running = false

    /**
     * The stopwatch instance
     */
    val stopwatch = Stopwatch.createStarted()

    // yaml must load first bc cache uses it
    init {
        YAMLParser.load()
        GameCycleWorker.start()
    }

    /**
     * Runs the server
     */
    fun run() {
        logger.info("Cache read from ${Cache.path}")
        logger.info(
            "${GameConstants.SERVER_NAME} v${GameConstants.BUILD_MAJOR}.${GameConstants.BUILD_MINOR} successfully booted in ${stopwatch.elapsed(
                TimeUnit.MILLISECONDS
            )} ms"
        )
        CodecRegistry.bindCodec()
        network.bind()
        running = true
    }

}