package world.gregs.voidps.engine.entity.character.mode.move.target

import org.rsmod.game.pathfinder.PathFinder
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.ModeType
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

data class NPCCharacterTargetStrategy(
    private val character: Character,
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = character.tile
    override val width: Int
        get() = character.size
    override val height: Int
        get() = character.size
    override val rotation = 0
    override val shape = -2
    override val sizeX: Int
        get() = character.size
    override val sizeY: Int
        get() = character.size

    override fun destination(source: Character): Tile {
        if (source is NPC) {
            val def = if (source.contains("transform_id")) {
                NPCDefinitions.get(source["transform_id", source.id])
            } else {
                source.def
            }
            if (def.walkMode.toInt() == ModeType.EMPTY) {
                return Tile.EMPTY
            }
        }
        return Tile(
            PathFinder.naiveDestination(
                sourceX = source.tile.x,
                sourceZ = source.tile.y,
                sourceWidth = source.size,
                sourceHeight = source.size,
                targetX = character.tile.x,
                targetZ = character.tile.y,
                targetWidth = character.size,
                targetHeight = character.size
            ).packed
        )
    }
}
