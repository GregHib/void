package org.redrune.engine

import org.redrune.engine.event.Event
import org.redrune.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class Startup : Event() {
    companion object : EventCompanion<Startup>()
}