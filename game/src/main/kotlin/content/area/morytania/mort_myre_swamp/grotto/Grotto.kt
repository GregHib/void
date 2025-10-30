package content.area.morytania.mort_myre_swamp.grotto

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Grotto : Script {

    init {
        objectOperate("Jump", "grotto_bridge") {
            player.walkToDelay(target.tile)
            character.clear("face_entity")
            val direction = if (player.tile.y > 3330) Direction.SOUTH else Direction.NORTH
            if (Level.success(player.levels.get(Skill.Agility), 60..252)) {
                player.anim("stepping_stone_step", delay = 30)
                player.exactMoveDelay(target.tile.addY(direction.delta.y * 2), startDelay = 58, delay = 70, direction = direction)
                player.sound("jump")
                player.exp(Skill.Agility, 15.0)
            } else {
                player.anim("rope_walk_fall_${if (direction == Direction.SOUTH) "right" else "left"}")
                var river = Tile(3439, 3330)
                areaGfx("big_splash", player.tile, delay = 3)
                player.sound("pool_plop")
                player.exactMoveDelay(river, startDelay = 20, delay = 40, direction = Direction.WEST)
                player.renderEmote("swim")
                river = river.add(Direction.WEST)
                player.face(river)
                player.walkOverDelay(river)
                river = river.add(direction)
                player.face(river)
                player.walkOverDelay(river)
                delay()
                player.clearRenderEmote()
                player.walkOverDelay(river.add(direction))
                player.message("You nearly drown in the disgusting swamp.")
                player.damage(random.nextInt(70))
                player.exp(Skill.Agility, 2.0)
            }
        }
    }
}
