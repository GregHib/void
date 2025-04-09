package content.skill.agility.shortcut

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Squeeze-through", "pipe_brimhaven_dungeon") {
    if (!player.has(Skill.Agility, 22)) {
        player.message("You need an Agility level of 22 to squeeze through the pipe.")
        return@objectOperate
    }
    val y = if (player.tile.y >= 9570) 9573 else 9566
    val targetTile = target.tile.copy(y = if (player.tile.y >= 9570) 9566 else 9573)
    player.walkToDelay(target.tile.copy(y = y))
    val direction = if (player.tile.y >= 9570) Direction.SOUTH else Direction.NORTH
    player.face(direction)
    player.anim("climb_through_pipe", delay = 30) // Not the correct anims but made it work
    player.exactMoveDelay(Tile(2655, 9569), startDelay = 30, delay = 126, direction = direction)
    player.tele(2655, targetTile.y - direction.delta.y * 2)
    player.anim("climb_through_pipe", delay = 20)
    player.exactMoveDelay(targetTile, delay = 96, direction = direction)
    player.exp(Skill.Agility, 8.5)
}