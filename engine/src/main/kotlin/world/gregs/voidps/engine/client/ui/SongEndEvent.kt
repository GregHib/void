package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class SongEndEvent(
    val songIndex: Int,
) : Event {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "song_end"
        1 -> songIndex
        else -> null
    }
}

fun songEnd(handler: suspend SongEndEvent.(Player) -> Unit) {
    Events.handle("song_end", "*", handler = handler)
}
