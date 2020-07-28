package rs.dusk.engine.model.world

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

data class ReloadChunk(val chunk: Chunk) : Event<Unit>() {
    companion object : EventCompanion<ReloadChunk>
}