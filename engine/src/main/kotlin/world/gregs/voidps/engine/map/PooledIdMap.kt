package world.gregs.voidps.engine.map

import kotlinx.io.pool.ObjectPool

open class PooledIdMap<Col : MutableCollection<Value>, Value : Any, Key : Id>(
    pool: ObjectPool<Col>
) : PooledIntMap<Col, Value>(pool) {

    fun add(k: Key, v: Value): Boolean {
        return super.add(k.id, v)
    }

    fun remove(k: Key, v: Value): Boolean {
        return super.remove(k.id, v)
    }

    operator fun get(key: Key): Col? {
        return super.get(key.id)
    }

    fun containsKey(k: Key): Boolean {
        return super.containsKey(k.id)
    }
}