package content.skill.agility.shortcut

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction

class Pipes : Script {

    init {
        objectOperate("Squeeze-through", "brimhaven_pipe_moss") { (target) ->
            if (target.tile.y == 9567 && !has(Skill.Agility, 22)) {
                message("You need an Agility level of 22 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughVertical(target, 9573, 9570, 9566, 8.5)
        }

        objectOperate("Squeeze-through", "brimhaven_pipe_dragon") { (target) ->
            if (target.tile.y == 9498 && !has(Skill.Agility, 34)) {
                message("You need an Agility level of 34 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughVertical(target, 9499, 9495, 9492, 10.0)
        }

        objectOperate("Squeeze-through", "varrock_dungeon_pipe") { (target) ->
            if (!has(Skill.Agility, 51)) {
                message("You need an Agility level of 51 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughHorizontal(target, 3149, 3152, 3155, 10.0)
        }
    }

    suspend fun Player.squeezeThroughVertical(target: GameObject, north: Int, middle: Int, south: Int, exp: Double) {
        val above = tile.y >= middle
        val y = if (above) north else south
        val targetTile = target.tile.copy(y = if (above) south else north)
        walkToDelay(target.tile.copy(y = y))
        val direction = if (above) Direction.SOUTH else Direction.NORTH
        face(direction)
        anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
        exactMoveDelay(target.tile.copy(y = middle), startDelay = 30, delay = 126, direction = direction)
        tele(target.tile.x, targetTile.y - direction.delta.y * 2)
        anim("climb_through_pipe", delay = 20)
        exactMoveDelay(targetTile, delay = 96, direction = direction)
        exp(Skill.Agility, exp)
    }

    suspend fun Player.squeezeThroughHorizontal(target: GameObject, east: Int, middle: Int, west: Int, exp: Double) {
        val right = tile.x < middle
        val targetTile = target.tile.copy(if (right) west else east)
        walkToDelay(target.tile.copy(if (right) east else west))
        val direction = if (right) Direction.EAST else Direction.WEST
        face(direction)
        anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
        exactMoveDelay(target.tile.copy(middle), startDelay = 30, delay = 126, direction = direction)
        tele(targetTile.x - direction.delta.x * 2, target.tile.y)
        anim("climb_through_pipe")
        exactMoveDelay(targetTile, delay = 96, direction = direction)
        exp(Skill.Agility, exp)
    }
}
