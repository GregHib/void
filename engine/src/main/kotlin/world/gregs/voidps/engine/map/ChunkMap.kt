package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.map.chunk.Chunk

class ChunkMap<T : Any>(
    capacity: Int
) : PooledIdMap<ObjectLinkedOpenHashSet<T>, T, Chunk>(
    pool = object : DefaultPool<ObjectLinkedOpenHashSet<T>>(capacity) {

        override fun produceInstance(): ObjectLinkedOpenHashSet<T> = ObjectLinkedOpenHashSet()

        override fun clearInstance(instance: ObjectLinkedOpenHashSet<T>): ObjectLinkedOpenHashSet<T> {
            instance.clear()
            instance.trim()
            return instance
        }
    }
)