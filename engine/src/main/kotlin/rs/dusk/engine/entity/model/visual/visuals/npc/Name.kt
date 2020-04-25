package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
inline class Name(val name: String) : Visual {
    companion object : VisualCompanion<Name>()
}