package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.map.region.RegionPlane

class RegionMap<T : Any>(
    capacity: Int
) : PooledIdMap<ObjectLinkedOpenHashSet<T>, T, RegionPlane>(
    pool = object : DefaultPool<ObjectLinkedOpenHashSet<T>>(capacity) {

        override fun produceInstance(): ObjectLinkedOpenHashSet<T> = ObjectLinkedOpenHashSet(8)

    }
)