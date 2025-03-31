package content.skill.agility.shortcut

import content.entity.gfx.areaGfx
import content.entity.sound.sound
import content.skill.firemaking.Light
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectApproach("Jump-across", "lumbridge_swamp_stepping_stone") {
    val direction = if (player.tile.x > target.tile.x) Direction.WEST else Direction.EAST
    val start = if (direction == Direction.WEST) Tile(3208, 9572) else Tile(3204, 9572)
    val end = if (direction == Direction.WEST) Tile(3204, 9572) else Tile(3208, 9572)
    if (player.tile != start) {
        player.walkToDelay(start)
        delay()
    }
    player.anim("stepping_stone_step", delay = 30)
    player.message(text = "You leap across with a mighty leap!", ChatType.Filter)
    player.sound("jump", delay = 35)
    player.exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
    if (Level.success(player.levels.get(Skill.Agility), 51..252)) {
        player.anim("stepping_stone_step", delay = 30)
        player.sound("jump", delay = 35)
        player.exactMoveDelay(end, startDelay = 58, delay = 70, direction = direction)
        player.exp(Skill.Agility, 3.0)
    } else {
        player.message("You slip over on the slimy stone.", ChatType.Filter)
        player.anim("rope_walk_fall_down")
        areaGfx("big_splash", target.tile.addY(direction.delta.x), delay = 6)
        player.sound("grapple_splash", 3)
        player.exactMoveDelay(target.tile.addY(direction.delta.x), startDelay = 12, delay = 40, direction = direction)
        val hasLightSource = Light.hasLightSource(player)
        if (hasLightSource) {
            Light.extinguish(player)
        }
        player.renderEmote("drowning")
        delay(2)
        player.exactMoveDelay(end, direction = direction)
        if (hasLightSource) {
            player.message("You scramble out of the muddy water.", ChatType.Filter)
        }
        player.clearRenderEmote()
    }
}
