package world.gregs.voidps.engine.entity.gfx

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.network.visual.update.Graphic

data class AreaGraphic(override var tile: Tile, val graphic: Graphic, val owner: String? = null) : Entity {
    val id = graphic.id

    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override var values: Values? = null
    var update: ChunkUpdate? = null
}