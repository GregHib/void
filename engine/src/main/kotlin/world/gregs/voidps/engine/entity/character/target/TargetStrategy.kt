package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

interface TargetStrategy {
    val bitMask: Int
    val tile: Tile
    val size: Size
    val rotation: Int
    val exitStrategy: Int
}