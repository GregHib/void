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