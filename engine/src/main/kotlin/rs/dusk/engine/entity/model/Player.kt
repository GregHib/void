package rs.dusk.engine.entity.model

import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.engine.entity.model.visual.visuals.player.getAppearance
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
    @Transient override val visuals: Visuals = Visuals(),
    @Transient override val changes: Changes = Changes(),
    @Transient override val movement: Movement = Movement()
) : Entity, Movable, Indexed {
    @Transient
    override var index: Int = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Player
        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "Player(${getAppearance().displayName}, tile=$tile)"
    }
}