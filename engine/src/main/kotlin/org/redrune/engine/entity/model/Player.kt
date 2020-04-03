package org.redrune.engine.entity.model

import org.redrune.engine.model.Tile

/**
 * A player controlled by client or bot
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Player(override var id: Int = -1, override var tile: Tile) : Entity, Movable {
    @Transient
    var index: Int = -1
}