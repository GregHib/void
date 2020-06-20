package rs.dusk.engine.action

import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

object Contexts {
    private val threads = Runtime.getRuntime().availableProcessors()
    val File = newFixedThreadPoolContext((threads / 4) - 1, "File IO")
    val Updating = newFixedThreadPoolContext(threads / 4, "Client Updating")
    val Pathing = newFixedThreadPoolContext(threads / 4, "Pathfinding")
    val Engine = newSingleThreadContext("Engine")
}