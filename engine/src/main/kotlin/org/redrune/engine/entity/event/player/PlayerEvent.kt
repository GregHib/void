package org.redrune.engine.entity.event.player

import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class PlayerEvent : Event() {
    abstract val player: Player
}