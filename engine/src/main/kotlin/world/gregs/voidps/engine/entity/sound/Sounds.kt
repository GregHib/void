package world.gregs.voidps.engine.entity.sound

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.list.BatchList

class Sounds(
    override val chunks: MutableMap<Int, MutableList<AreaSound>> = Int2ObjectOpenHashMap()
) : BatchList<AreaSound>