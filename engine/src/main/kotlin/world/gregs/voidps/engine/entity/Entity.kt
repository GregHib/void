package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile

    val size: Int
        get() = when (this) {
            is NPC -> def.size
            is Player -> appearance.size
            else -> 1
        }
}

fun Tile.distanceTo(entity: Entity) = when (entity) {
    is Character -> distanceTo(entity.tile, entity.size, entity.size)
    is GameObject -> distanceTo(entity.tile, entity.width, entity.height)
    else -> distanceTo(entity.tile)
}