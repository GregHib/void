package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
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
            // Line up on the centre lane just outside the passage before squeezing through
            val start = if (sideways) {
                val x = if (direction == Direction.EAST) target.tile.x - 1 else target.tile.x + target.width
                val centre = tile.y.coerceIn(target.tile.y + (target.height - 1) / 2, target.tile.y + target.height / 2)
                Tile(x, centre, tile.level)
            } else {
                val y = if (direction == Direction.NORTH) target.tile.y - 1 else target.tile.y + target.height
                val centre = tile.x.coerceIn(target.tile.x + (target.width - 1) / 2, target.tile.x + target.width / 2)
                Tile(centre, y, tile.level)
            }
            if (tile != start) {
                walkToDelay(start, forceWalk = true)
                if (tile != start) {
                    return@objectOperate
                }
            }
            val dest = tile.add(direction.delta.x * 3, direction.delta.y * 3)
            face(direction)
            when (target.id) {
                "dense_forest", "dense_forest_hard_2" -> {
                    anim("dense_forest_climb", delay = 30)
                    sound("forest_lowwall", delay = 30)
                }
                "dense_forest_2", "dense_forest_hard" -> {
                    anim("dense_forest_double_squeeze", delay = 30)
                    sound("forest_doublesqueeze", delay = 30)
                }
                else -> {
                    anim("dense_forest_squeeze", delay = 30)
                    sound("forest_sidesqueeze", delay = 40)
                }
            }
            exactMoveDelay(dest, delay = 94, direction = direction, startDelay = 30)
            if (target.id == "dense_forest_2" || target.id == "dense_forest_hard") {
                clearAnim()
            }
            // Interacting faced the object, which is now behind; keep facing the way travelled
            face(direction)
        }
    }
}
