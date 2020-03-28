package org.redrune.engine.entity.tile

import org.koin.dsl.module
import org.redrune.engine.entity.model.Entity
import org.redrune.engine.model.Tile
import org.redrune.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
interface Tiles {
    operator fun get(entity: Entity): Tile

    operator fun set(entity: Entity, tile: Tile)

    fun contains(entity: Entity): Boolean
}

val tileModule = module {
    single { TileList() as Tiles }
}

fun Entity.tile(): Tile = get<Tiles>()[this]