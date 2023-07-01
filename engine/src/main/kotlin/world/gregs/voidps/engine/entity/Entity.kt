package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile
}

fun Tile.distanceTo(entity: Entity) = when (entity) {
    is Character -> distanceTo(entity.tile, entity.size.width, entity.size.height)
    is GameObject -> distanceTo(entity.tile, entity.width, entity.height)
    else -> distanceTo(entity.tile)
}