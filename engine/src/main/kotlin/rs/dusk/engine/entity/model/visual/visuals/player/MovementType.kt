package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
sealed class MovementType(val type: Int) : Visual {
    object None : MovementType(0)
    object Walk : MovementType(1)
    object Run : MovementType(2)
    object Teleport : MovementType(127)

    companion object : VisualCompanion<MovementType>()
}