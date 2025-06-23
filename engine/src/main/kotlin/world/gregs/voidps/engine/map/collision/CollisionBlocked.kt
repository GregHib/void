package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

fun Character.blocked(direction: Direction) = blocked(tile, direction)

fun Character.blocked(tile: Tile, direction: Direction): Boolean {
    val flag = if (this is NPC) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
    val size = if (this is NPC) {
        def.size
    } else if (this is Player) {
        appearance.size
    } else {
        1
    }
    return !get<StepValidator>().canTravel(
        x = tile.x,
        z = tile.y,
        level = tile.level,
        size = size,
        offsetX = direction.delta.x,
        offsetZ = direction.delta.y,
        extraFlag = flag,
        collision = collision,
    )
}
