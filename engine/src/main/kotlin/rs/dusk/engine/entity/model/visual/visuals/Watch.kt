package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Watch(var index: Int = -1) : Visual {

    constructor(npc: NPC) : this(npc.index)

    constructor(player: Player) : this(player.index or 0x8000)
}