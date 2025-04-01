package content.area.karamja

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Swing-on", "brimhaven_ropeswing_west") {
    player.face(target.tile.add(Direction.WEST))
    if (!player.has(Skill.Agility, 10)) {
        player.message("You need an agility level of 10 to attempt to swing on this vine.")
        return@objectOperate
    }
    player.walkToDelay(Tile(2709, 3209))
    player.anim("rope_swing")
    target.anim("swing_rope")
    player.exactMoveDelay(Tile(2704, 3209), startDelay = 45, delay = 70, direction = Direction.WEST)
    player.exp(Skill.Agility, 3.0)
    player.message("You skillfully swing across.", ChatType.Filter)
}

objectOperate("Swing-on", "brimhaven_ropeswing_east") {
    player.face(target.tile.add(Direction.EAST))
    if (!player.has(Skill.Agility, 10)) {
        player.message("You need an agility level of 10 to attempt to swing on this vine.")
        return@objectOperate
    }
    player.walkToDelay(Tile(2705, 3205))
    player.anim("rope_swing")
    target.anim("swing_rope")
    player.exactMoveDelay(Tile(2709, 3205), startDelay = 45, delay = 70, direction = Direction.EAST)
    player.exp(Skill.Agility, 3.0)
    player.message("You skillfully swing across.", ChatType.Filter)
}

val steps = mutableListOf(Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH)

objectOperate("Jump-from", "brimhaven_stepping_stones_start") {
    player.face(target.tile.add(Direction.WEST))
    if (!player.has(Skill.Agility, 12)) {
        player.message("You need an agility level of 12 to attempt to swing on this vine.")
        return@objectOperate
    }
    player.message("You carefully start crossing the stepping stones...", ChatType.Filter)
    for (direction in steps) {
        player.anim("stepping_stone_jump")
        player.exactMoveDelay(player.tile.add(direction), direction = direction)
        player.face(direction)
        delay()
    }
    player.exp(Skill.Agility, 7.5)
    player.message("... You safely cross to the other side.", ChatType.Filter)
}

objectOperate("Jump-from", "brimhaven_stepping_stones_end") {
    player.face(target.tile.add(Direction.EAST))
    player.message("You carefully start crossing the stepping stones...", ChatType.Filter)
    for (i in steps.indices.reversed()) {
        val direction = steps[i].inverse()
        player.anim("stepping_stone_jump")
        player.exactMoveDelay(player.tile.add(direction), direction = direction)
        player.face(direction)
        delay()
    }
    player.exp(Skill.Agility, 7.5)
    player.message("... You safely cross to the other side.", ChatType.Filter)
}
