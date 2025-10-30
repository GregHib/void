package content.area.kharidian_desert.al_kharid

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class AlKharidMine : Script {

    init {
        objectOperate("Climb", "al_kharid_mine_shortcut_bottom") {
            if (!has(Skill.Agility, 38)) {
                message("You need an Agility level of 38 to negotiate these rocks.")
                return@objectOperate
            }
            face(Direction.EAST)
            delay()
            walkToDelay(Tile(3303, 3315))
            renderEmote("climbing")
            walkOverDelay(Tile(3307, 3315))
            clearRenderEmote()
        }

        objectOperate("Climb", "al_kharid_mine_shortcut_top") {
            if (!has(Skill.Agility, 38)) {
                message("You need an Agility level of 38 to negotiate these rocks.")
                return@objectOperate
            }
            face(Direction.EAST)
            delay()
            walkOverDelay(Tile(3305, 3315))
            face(Direction.WEST)
            delay()
            anim("human_climbing_down", delay = 10)
            exactMoveDelay(Tile(3303, 3315), delay = 120, direction = Direction.EAST)
        }
    }
}
