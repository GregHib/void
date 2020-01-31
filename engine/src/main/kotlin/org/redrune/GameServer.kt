package org.redrune

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.redrune.cache.Cache
import org.redrune.network.NetworkInitializer
import org.redrune.tools.YAMLParser
import org.redrune.tools.constants.GameConstants.Companion.BUILD_MAJOR
import org.redrune.tools.constants.GameConstants.Companion.BUILD_MINOR
import org.redrune.tools.constants.GameConstants.Companion.SERVER_NAME
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object GameServer {

    private val logger = InlineLogger()

    /**
     * If the game server is running
     */
    var running = false

    /**
     * The stopwatch instance
     */
    val stopwatch: Stopwatch = Stopwatch.createStarted()

    /**
     * Runs the server
     */
    fun run() {
        YAMLParser.load()
        Cache.load()
        NetworkInitializer.bind()
        logger.info { "$SERVER_NAME v$BUILD_MAJOR.$BUILD_MINOR successfully booted in ${stopwatch.elapsed(MILLISECONDS)} ms" }
        running = true
    }

}