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
            if (!player.has(Skill.Agility, 38)) {
                player.message("You need an Agility level of 38 to negotiate these rocks.")
                return@objectOperate
            }
            player.face(Direction.EAST)
            delay()
            player.walkToDelay(Tile(3303, 3315))
            player.renderEmote("climbing")
            player.walkOverDelay(Tile(3307, 3315))
            player.clearRenderEmote()
        }

        objectOperate("Climb", "al_kharid_mine_shortcut_top") {
            if (!player.has(Skill.Agility, 38)) {
                player.message("You need an Agility level of 38 to negotiate these rocks.")
                return@objectOperate
            }
            player.face(Direction.EAST)
            delay()
            player.walkOverDelay(Tile(3305, 3315))
            player.face(Direction.WEST)
            delay()
            player.anim("human_climbing_down", delay = 10)
            player.exactMoveDelay(Tile(3303, 3315), delay = 120, direction = Direction.EAST)
        }
    }
}
