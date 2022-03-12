package world.gregs.voidps.engine.entity.gfx

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.list.BatchList

class Graphics(
    override val chunks: MutableMap<Int, MutableList<AreaGraphic>> = Int2ObjectOpenHashMap()
) : BatchList<AreaGraphic>