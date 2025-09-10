package content.quest.member.eagles_peak

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.engine.event.Script
@Script
class EaglesEyries {

    init {
        objectOperate("Climb", "eagles_peak_rocks") {
            if (!player.has(Skill.Agility, 25)) {
                player.message("You must have an Agility level of at least 25 to climb these rocks.")
                return@objectOperate
            }
            val up = target.tile.equals(2322, 3501)
            if (up) {
                player.anim("rocks_climb", delay = 15)
                player.exactMove(Tile(2324, 3497), 250, Direction.SOUTH)
                delay(7)
                player.anim("human_walk_forward")
                delay()
            } else {
                player.anim("human_walk_backwards", delay = 7)
                player.exactMove(Tile(2322, 3502), 240, Direction.SOUTH)
                delay()
                player.anim("rocks_climb_down")
                delay(7)
            }
            player.clearAnim()
            player.exp(Skill.Agility, 1.0)
        }

        objectOperate("Climb", "rocky_handholds_bottom") {
            if (!player.has(Skill.Agility, 35)) {
                player.message("You must have an Agility level of at least 35 to climb these rocks.")
                return@objectOperate
            }
            delay()
            player.renderEmote("climbing")
            player.walkOverDelay(Tile(2744, 3830, 1))
            player.clearRenderEmote()
            player.exp(Skill.Agility, 1.0)
        }

        objectOperate("Climb", "rocky_handholds_top") {
            if (!player.has(Skill.Agility, 35)) {
                player.message("You must have an Agility level of at least 35 to climb these rocks.")
                return@objectOperate
            }
            player.anim("rocks_climb_down", delay = 15)
            player.exactMoveDelay(Tile(2740, 3830, 1), 90, Direction.EAST)
            player.clearAnim()
            player.exp(Skill.Agility, 1.0)
        }

    }

}
