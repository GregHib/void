package content.skill.agility.shortcut

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction

@Script
class Pipes {

    init {
        objectOperate("Squeeze-through", "brimhaven_pipe_moss") {
            if (target.tile.y == 9567 && !player.has(Skill.Agility, 22)) {
                player.message("You need an Agility level of 22 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughVertical(9573, 9570, 9566, 8.5)
        }

        objectOperate("Squeeze-through", "brimhaven_pipe_dragon") {
            if (target.tile.y == 9498 && !player.has(Skill.Agility, 34)) {
                player.message("You need an Agility level of 34 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughVertical(9499, 9495, 9492, 10.0)
        }

        objectOperate("Squeeze-through", "varrock_dungeon_pipe") {
            if (!player.has(Skill.Agility, 51)) {
                player.message("You need an Agility level of 51 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughHorizontal(3149, 3152, 3155, 10.0)
        }
    }

    suspend fun ObjectOption<Player>.squeezeThroughVertical(north: Int, middle: Int, south: Int, exp: Double) {
        val above = player.tile.y >= middle
        val y = if (above) north else south
        val targetTile = target.tile.copy(y = if (above) south else north)
        player.walkToDelay(target.tile.copy(y = y))
        val direction = if (above) Direction.SOUTH else Direction.NORTH
        player.face(direction)
        player.anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
        player.exactMoveDelay(target.tile.copy(y = middle), startDelay = 30, delay = 126, direction = direction)
        player.tele(target.tile.x, targetTile.y - direction.delta.y * 2)
        player.anim("climb_through_pipe", delay = 20)
        player.exactMoveDelay(targetTile, delay = 96, direction = direction)
        player.exp(Skill.Agility, exp)
    }

    suspend fun ObjectOption<Player>.squeezeThroughHorizontal(east: Int, middle: Int, west: Int, exp: Double) {
        val right = player.tile.x < middle
        val targetTile = target.tile.copy(if (right) west else east)
        player.walkToDelay(target.tile.copy(if (right) east else west))
        val direction = if (right) Direction.EAST else Direction.WEST
        player.face(direction)
        player.anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
        player.exactMoveDelay(target.tile.copy(middle), startDelay = 30, delay = 126, direction = direction)
        player.tele(targetTile.x - direction.delta.x * 2, target.tile.y)
        player.anim("climb_through_pipe")
        player.exactMoveDelay(targetTile, delay = 96, direction = direction)
        player.exp(Skill.Agility, exp)
    }
}
