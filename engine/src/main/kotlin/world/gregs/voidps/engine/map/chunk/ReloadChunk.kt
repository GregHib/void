package world.gregs.voidps.engine.map.chunk

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion

data class ReloadChunk(val chunk: Chunk) : Event {
    companion object : EventCompanion<ReloadChunk>
}