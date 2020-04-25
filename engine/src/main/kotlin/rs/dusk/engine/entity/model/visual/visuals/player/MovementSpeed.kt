package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
sealed class MovementSpeed(val value: Boolean) : Visual {
    object Walk : MovementSpeed(false)
    object Run : MovementSpeed(true)
    companion object : VisualCompanion<MovementSpeed>()
}