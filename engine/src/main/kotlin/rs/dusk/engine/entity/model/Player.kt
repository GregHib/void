package rs.dusk.engine.entity.model

import rs.dusk.engine.model.Tile
import rs.dusk.engine.view.Viewport

/**
 * A player controlled by client or bot
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Player(override var id: Int = -1, override var tile: Tile, val viewport: Viewport = Viewport()) : Entity,
    Movable {
    @Transient
    var index: Int = -1
}