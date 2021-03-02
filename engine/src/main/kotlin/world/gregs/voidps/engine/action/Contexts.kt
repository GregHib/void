package world.gregs.voidps.engine.action

import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

object Contexts {
    private val threads = Runtime.getRuntime().availableProcessors()
    val File = newFixedThreadPoolContext(1, "File IO")
    val Updating = newFixedThreadPoolContext(threads - 2, "Client Updating")
//    val Pathing = newFixedThreadPoolContext(threads / 4, "Pathfinding")
    val Game = newSingleThreadContext("Game loop")
}