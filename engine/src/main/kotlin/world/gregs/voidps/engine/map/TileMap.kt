package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

class TileMap<T : Any>(
    capacity: Int,
    pool: ObjectPool<ObjectLinkedOpenHashSet<T>> = object : DefaultPool<ObjectLinkedOpenHashSet<T>>(capacity) {

        override fun produceInstance(): ObjectLinkedOpenHashSet<T> = ObjectLinkedOpenHashSet()

        override fun clearInstance(instance: ObjectLinkedOpenHashSet<T>): ObjectLinkedOpenHashSet<T> {
            instance.clear()
            instance.trim()
            return instance
        }
    }
) : PooledIdMap<ObjectLinkedOpenHashSet<T>, T, Tile>(pool)