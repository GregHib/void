package world.gregs.voidps.engine.entity.gfx

import world.gregs.voidps.engine.entity.list.BatchList
import world.gregs.voidps.engine.map.chunk.Chunk

class Graphics(
    override val chunks: MutableMap<Chunk, MutableSet<AreaGraphic>> = mutableMapOf()
) : BatchList<AreaGraphic>