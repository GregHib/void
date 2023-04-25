package world.gregs.voidps.engine

import kotlinx.coroutines.newSingleThreadContext

object Contexts {
    val Game = newSingleThreadContext("Game loop")
}