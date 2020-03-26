package org.redrune.engine.event

import kotlinx.coroutines.channels.SendChannel

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
class EventHandler<T : Event> {
    var next: EventHandler<T>? = null
    lateinit var actor: SendChannel<T>
}

