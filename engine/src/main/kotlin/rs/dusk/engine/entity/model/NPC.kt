package rs.dusk.engine.entity.model

import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.engine.model.Tile

/**
 * A non-player character
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class NPC(
    override val id: Int,
    override var tile: Tile,
    override val visuals: Visuals = Visuals(),
    override val changes: Changes = Changes(),
    override val movement: Movement = Movement()
) : Entity, Indexed {
    override var index: Int = -1
}