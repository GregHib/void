package org.redrune.engine

import org.redrune.GameServer
import java.util.concurrent.Executors

/**
 * This worker processes all game actions at the 600ms tick
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object GameCycleWorker : Runnable {

    private var lastCycleTime: Long = 0
    private var cyclesPassed: Long = 0
    private var started = false

    override fun run() {
        while (GameServer.running) {
            val currentTime: Long = System.currentTimeMillis()
            try {
//                updateSequence.fire(World.getLobbyPlayers(), World.getPlayers(), World.getNPCs())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            sleepThread(currentTime)
        }
    }

    /**
     * Handles the sleeping of the thread
     */
    private fun sleepThread(startTime: Long) {
        lastCycleTime = System.currentTimeMillis()
        val sleepTime: Long = 600 + (startTime - lastCycleTime)
        if (sleepTime <= 0) {
            return
        }
        cyclesPassed++
        try {
            Thread.sleep(sleepTime)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun start() {
        if (started) {
            return
        }
        started = true
        Executors.newSingleThreadExecutor().execute(this@GameCycleWorker)
    }
}