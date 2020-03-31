package org.redrune.engine.entity.tile

import org.koin.core.context.loadKoinModules
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.priority
import org.redrune.engine.event.then
import org.redrune.engine.event.where
import org.redrune.engine.model.Tile
import org.redrune.utility.getProperty
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 29, 2020
 */

loadKoinModules(tileModule)

val tiles: Tiles by inject()
val x = getProperty("homeX", 0)
val y = getProperty("homeY", 0)
val plane = getProperty("homePlane", 0)

// New player home spawn
Registered priority 10 where { entity is Player } then {
    if (!tiles.contains(entity)) {
        tiles[entity] = Tile(x, y, plane)
    }
}