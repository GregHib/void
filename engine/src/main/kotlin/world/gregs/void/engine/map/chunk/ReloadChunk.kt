package world.gregs.void.engine.map.chunk

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

data class ReloadChunk(val chunk: Chunk) : Event<Unit>() {
    companion object : EventCompanion<ReloadChunk>
}