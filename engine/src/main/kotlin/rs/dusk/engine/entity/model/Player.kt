package rs.dusk.engine.entity.model

import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.engine.model.Tile
import rs.dusk.engine.view.Viewport

/**
 * A player controlled by client or bot
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Player(
    override var id: Int = -1,
    override var tile: Tile,
    @Transient val viewport: Viewport = Viewport(),
    @Transient override val visuals: Visuals = Visuals()
) : Entity, Movable, Indexed {
    @Transient
    override var index: Int = -1
}