package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.ints.IntArrayList
import kotlinx.io.pool.DefaultPool

class TileMap(capacity: Int) : PooledIdMap<IntArrayList, Int, Tile>(
    object : DefaultPool<IntArrayList>(capacity) {
        override fun produceInstance() = IntArrayList(8)
    }
)