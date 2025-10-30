package content.area.misthalin.edgeville

import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class EdgevilleMonkeyBars : Script {

    init {
        objectOperate("Swing across", "edgeville_monkey_bars") { (target) ->
            if (!has(Skill.Agility, 15)) {
                message("You need an Agility level of 15 to swing across the bars.")
                return@objectOperate
            }
            val north = tile.y > 9967
            val y = if (north) 9964 else 9969
            val x = if (target.tile.x == 3119) 3120 else 3121
            walkToDelay(Tile(x, target.tile.y))
            clear("face_entity")
            face(if (north) Direction.SOUTH else Direction.NORTH)
            delay()
            sound("monkeybars_on")
            anim("jump_onto_monkey_bars")
            renderEmote("monkey_bars")
            delay(2)
            sound("monkeybars_loop", repeat = 11)
            walkOverDelay(Tile(x, y), forceWalk = true)
            delay()
            clearRenderEmote()
            anim("jump_from_monkey_bars")
            sound("monkeybars_off")
            exp(Skill.Agility, 20.0)
        }
    }
}
