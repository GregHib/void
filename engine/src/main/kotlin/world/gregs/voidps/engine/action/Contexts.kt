package world.gregs.voidps.engine.action

import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

object Contexts {
    private val threads = Runtime.getRuntime().availableProcessors()
    val Updating = newFixedThreadPoolContext(threads - 2, "Client Updating")
    val Game = newSingleThreadContext("Game loop")
}