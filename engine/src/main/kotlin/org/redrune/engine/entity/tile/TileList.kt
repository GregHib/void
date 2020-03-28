package org.redrune.engine.entity.tile

import org.redrune.engine.entity.model.Entity
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 29, 2020
 */
class TileList : Tiles {
    private val delegate: MutableMap<Entity, Tile> = mutableMapOf()

    override fun get(entity: Entity): Tile {
        return delegate[entity]!!
    }

    override fun set(entity: Entity, tile: Tile) {
        delegate[entity] = tile
    }

    override fun contains(entity: Entity): Boolean {
        return delegate.containsKey(entity)
    }
}