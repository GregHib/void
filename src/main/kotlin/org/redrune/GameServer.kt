package org.redrune

import org.redrune.cache.Cache
import org.redrune.engine.GameCycleWorker
import org.redrune.network.NetworkBinder
import org.redrune.util.YAMLParser

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

    // yaml must load first bc cache uses it
    init {
        yamlParser.load()
        cache = Cache
    }

    /**
     * Runs the server
     */
    fun run() {
        println("Loaded ${yamlParser.getSize()} configurations.")
        println("Cache read from ${cache.path}.")
        network.init()
    }
}