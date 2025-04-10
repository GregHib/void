package content.skill.agility.shortcut

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

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

suspend fun ObjectOption<Player>.squeezeThroughVertical(north: Int, middle: Int, south: Int, exp: Double) {
    val y = if (player.tile.y >= middle) north else south
    val targetTile = target.tile.copy(y = if (player.tile.y >= middle) south else north)
    player.walkToDelay(target.tile.copy(y = y))
    val direction = if (player.tile.y >= middle) Direction.SOUTH else Direction.NORTH
    player.face(direction)
    player.anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
    player.exactMoveDelay(target.tile.copy(y = middle), startDelay = 30, delay = 126, direction = direction)
    player.tele(target.tile.x, targetTile.y - direction.delta.y * 2)
    player.anim("climb_through_pipe", delay = 20)
    player.exactMoveDelay(targetTile, delay = 96, direction = direction)
    player.exp(Skill.Agility, exp)
}