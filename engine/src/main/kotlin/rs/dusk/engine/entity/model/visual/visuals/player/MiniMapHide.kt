package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
inline class MiniMapHide(val hide: Boolean = false) : Visual {
    companion object : VisualCompanion<MiniMapHide>()
}