package world.gregs.voidps.engine.entity.sound

import world.gregs.voidps.engine.entity.list.BatchList
import world.gregs.voidps.engine.map.chunk.Chunk

class Sounds(
    override val chunks: MutableMap<Chunk, MutableSet<AreaSound>> = mutableMapOf()
) : BatchList<AreaSound>