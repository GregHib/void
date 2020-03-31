package org.redrune.engine.player

import org.redrune.engine.entity.event.player.PlayerRequest

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
interface PlayerController {

    fun send(request: PlayerRequest)

}