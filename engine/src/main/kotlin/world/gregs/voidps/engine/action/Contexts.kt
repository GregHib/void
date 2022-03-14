package world.gregs.voidps.engine.action

import kotlinx.coroutines.newSingleThreadContext

object Contexts {
    val Game = newSingleThreadContext("Game loop")
}