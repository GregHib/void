package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class DenseForest : Script {

    init {
        objectOperate("Enter", "dense_forest,dense_forest_2,dense_forest_3,dense_forest_hard,dense_forest_hard_2") { (target) ->
            val level = target.def["level", 0]
            if (level > 0 && !has(Skill.Agility, level)) {
                obstacle(level)
                return@objectOperate
            }
            // Rotation determines the passage's axis; squeeze through to the far side
            val sideways = target.rotation == 1 || target.rotation == 3
            val direction = if (sideways) {
                if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            } else {
                if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            }
            if (target.id == "dense_forest" || target.id == "dense_forest_2") {
                // Line up on the tile just before the passage's footprint before squeezing through
                val start = if (sideways) {
                    val x = if (direction == Direction.EAST) target.tile.x - 1 else target.tile.x + target.width
                    val centre = tile.y.coerceIn(target.tile.y + (target.height - 1) / 2, target.tile.y + target.height / 2)
                    Tile(x, centre, tile.level)
                } else {
                    val y = if (direction == Direction.NORTH) target.tile.y - 1 else target.tile.y + target.height
                    val centre = tile.x.coerceIn(target.tile.x + (target.width - 1) / 2, target.tile.x + target.width / 2)
                    Tile(centre, y, tile.level)
                }
                walkOverDelay(start)
            }
            val dest = tile.add(direction.delta.x * 3, direction.delta.y * 3)
            face(direction)
            delay()
            anim(if (target.id == "dense_forest") "dense_forest_climb" else "dense_forest_squeeze")
            exactMoveDelay(dest, delay = 90, direction = direction)
        }
    }
}
