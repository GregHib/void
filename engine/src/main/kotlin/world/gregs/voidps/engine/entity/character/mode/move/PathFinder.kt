package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.type.Tile

fun PathFinder.findPath(character: Character, strategy: TargetStrategy, shape: Int?) = findPath(
    srcX = character.tile.x,
    srcZ = character.tile.y,
    level = character.tile.level,
    destX = strategy.tile.x,
    destZ = strategy.tile.y,
    srcSize = character.size,
    destWidth = strategy.sizeX,
    destHeight = strategy.sizeY,
    objShape = shape ?: strategy.exitStrategy,
    objRot = strategy.rotation,
    blockAccessFlags = strategy.bitMask
)

fun StepValidator.canTravel(character: Character, x: Int, y: Int): Boolean {
    val flag = if (character is NPC && character.def["solid", true]) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
    return canTravel(
        level = character.tile.level,
        x = character.tile.x,
        z = character.tile.y,
        offsetX = x,
        offsetZ = y,
        size = character.size,
        extraFlag = flag,
        collision = character.collision
    )
}

fun LineValidator.hasLineOfSight(character: Character, target: Character) = hasLineOfSight(
    srcX = character.tile.x,
    srcZ = character.tile.y,
    level = character.tile.level,
    srcSize = character.size,
    destX = target.tile.x,
    destZ = target.tile.y,
    destWidth = target.size,
    destHeight = target.size
)

fun LineValidator.hasLineOfSight(character: Character, target: Tile, width: Int, height: Int) = hasLineOfSight(
    srcX = character.tile.x,
    srcZ = character.tile.y,
    level = character.tile.level,
    srcSize = character.size,
    destX = target.x,
    destZ = target.y,
    destWidth = width,
    destHeight = height
)

fun LineValidator.hasLineOfWalk(character: Character, target: Tile, width: Int, height: Int) = hasLineOfWalk(
    srcX = character.tile.x,
    srcZ = character.tile.y,
    level = character.tile.level,
    srcSize = character.size,
    destX = target.x,
    destZ = target.y,
    destWidth = width,
    destHeight = height
)