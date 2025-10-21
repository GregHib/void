package content.area.misthalin.edgeville

import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Script
class EdgevilleMonkeyBars {

    init {
        objectOperate("Swing across", "edgeville_monkey_bars") {
            if (!player.has(Skill.Agility, 15)) {
                player.message("You need an Agility level of 15 to swing across the bars.")
                return@objectOperate
            }
            val north = player.tile.y > 9967
            val y = if (north) 9964 else 9969
            val x = if (target.tile.x == 3119) 3120 else 3121
            player.walkToDelay(Tile(x, target.tile.y))
            player.clear("face_entity")
            player.face(if (north) Direction.SOUTH else Direction.NORTH)
            delay()
            player.sound("monkeybars_on")
            player.anim("jump_onto_monkey_bars")
            player.renderEmote("monkey_bars")
            delay(2)
            player.sound("monkeybars_loop", repeat = 11)
            player.walkOverDelay(Tile(x, y), forceWalk = true)
            delay()
            player.clearRenderEmote()
            player.anim("jump_from_monkey_bars")
            player.sound("monkeybars_off")
            player.exp(Skill.Agility, 20.0)
        }
    }
}
