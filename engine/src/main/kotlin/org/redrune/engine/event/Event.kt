package org.redrune.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
abstract class Event {
    var cancelled = false
        private set

    fun cancel() {
        cancelled = true
    }
}