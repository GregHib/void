package org.redrune

import com.google.common.base.Stopwatch
import org.redrune.cache.Cache
import org.redrune.engine.GameCycleWorker
import org.redrune.network.NetworkBinder
import org.redrune.util.YAMLParser
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object GameServer {

    /**
     * The
     */
    private val cache: Cache

    /**
     * The instance of the game thread
     */
    private val gameThread = GameCycleWorker

    /**
     * The instance of the network
     */
    private val network = NetworkBinder

    /**
     * The instance of the yaml parser
     */
    private val yamlParser = YAMLParser

    private val mainWorker = GameCycleWorker

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
        yamlParser.load()
        cache = Cache
        mainWorker.start()
    }

    /**
     * Runs the server
     */
    fun run() {
        println("Cache read from ${cache.path}")
        println("${GameConstants.SERVER_NAME} v${GameConstants.BUILD_MAJOR}.${GameConstants.BUILD_MINOR} successfully booted in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)} ms")
        running = network.init()
    }
}