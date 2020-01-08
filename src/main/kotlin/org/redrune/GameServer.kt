package org.redrune

import org.redrune.cache.Cache
import org.redrune.engine.GameCycleWorker
import org.redrune.network.NetworkBinder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object GameServer {

    /**
     * The
     */
    val cache = Cache

    /**
     * The instance of the game thread
     */
    private val gameThread = GameCycleWorker

    /**
     * The
     */
    private val network = NetworkBinder

    /**
     * Runs the server
     */
    fun run() {
        println("Cache reading from ${cache.path}.")
        network.init()
    }
}