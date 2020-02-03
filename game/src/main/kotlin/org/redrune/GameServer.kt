package org.redrune

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.redrune.cache.Cache
import org.redrune.network.NetworkInitializer
import org.redrune.tools.YAMLParser
import org.redrune.tools.constants.GameConstants.Companion.BUILD_MAJOR
import org.redrune.tools.constants.GameConstants.Companion.BUILD_MINOR
import org.redrune.tools.constants.GameConstants.Companion.SERVER_NAME
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
    val world: World
) {

    private val logger = InlineLogger()

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

    /**
     * Runs the server
     */
    fun run() {
        YAMLParser.load()
        Cache.load()
        network.init().bind()
        logger.info {
            "$SERVER_NAME v$BUILD_MAJOR.$BUILD_MINOR successfully booted world ${world.id} in ${stopwatch.elapsed(
                MILLISECONDS
            )} ms"
        }
        running = true
    }

}