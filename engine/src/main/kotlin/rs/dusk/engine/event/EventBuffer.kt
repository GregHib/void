package rs.dusk.engine.event

import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

/**
 * Hold inbound events for processing at the start of the next tick
 * @since July 10, 2020
 */
class EventBuffer(val eventLimit: Int, val bus: EventBus, val buffered: MutableMap<Player, MutableList<() -> Unit>> = mutableMapOf()) {

    /**
     * Removes [player]'s buffer list
     */
    fun remove(player: Player) = buffered.remove(player)

    /**
     * Buffers an event to be processed at the next tick
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : PlayerEvent> emitLater(event: T) {
        val list = buffered.getOrPut(event.player) { mutableListOf() }
        if(list.size < eventLimit) {
            list.add {
                bus.emit(event)
            }
        }
    }

}