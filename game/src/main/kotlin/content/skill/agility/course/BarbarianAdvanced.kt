package content.skill.agility.course

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class BarbarianAdvanced : Script {

    init {
        objectOperate("Run-up", "barbarian_outpost_run_wall") {
            if (!has(Skill.Agility, 90, message = true)) {
                return@objectOperate
            }
            clear("face_entity")
            delay()
            face(Direction.NORTH)
            anim("barbarian_wall_jump_climb")
            delay(7)
            anim("barbarian_wall_jump")
            exactMoveDelay(Tile(2538, 3545, 2), 30, Direction.NORTH)
            delay(1)
            exp(Skill.Agility, 15.0)
            agilityStage(3)
        }

        objectOperate("Climb-up", "barbarian_outpost_climb_wall") {
            clear("face_entity")
            walkToDelay(Tile(2537, 3546, 2))
            face(Direction.WEST)
            delay()
            anim("barbarian_wall_climb")
            delay()
            tele(2536, 3546, 3)
            anim("barbarian_wall_stand_up")
            delay()
            exp(Skill.Agility, 15.0)
            agilityStage(4)
        }

        objectOperate("Fire", "barbarian_outpost_spring") { (target) ->
            clear("face_entity")
            face(Direction.NORTH)
            delay(1)
            target.anim("barbarian_spring_fire")
            delay(1)
            tele(2533, 3547, 3)
            anim("barbarian_spring_shoot")
            exactMoveDelay(Tile(2532, 3553, 3), 60, Direction.NORTH)
            target.anim("barbarian_spring_reset")
            delay(2)
            exp(Skill.Agility, 15.0)
            agilityStage(5)
        }

        objectOperate("Cross", "barbarian_outpost_balance_beam") {
            face(Direction.EAST)
            delay()
            anim("circus_cartwheel")
            delay()
            exactMoveDelay(Tile(2536, 3553, 3), 45, Direction.EAST)
            renderEmote("beam_balance")
            delay()
            exp(Skill.Agility, 15.0)
            agilityStage(6)
        }

        objectOperate("Jump-over", "barbarian_outpost_gap") {
            clearRenderEmote()
            anim("jump_down")
            delay()
            tele(2539, 3553, 2)
            anim("jump_land")
            delay()
            exp(Skill.Agility, 15.0)
            agilityStage(7)
        }

        objectOperate("Slide-down", "barbarian_outpost_roof") {
            anim("barbarian_slide_start")
            exactMoveDelay(tile.copy(x = 2540), 30, Direction.EAST)
            anim("barbarian_slide")
            exactMove(tile.copy(x = 2543, level = 1), 90, Direction.EAST)
            delay()
            anim("barbarian_slide")
            delay()
            anim("barbarian_slide_jump")
            delay()
            tele(2544, tile.y, 0)
            anim("jump_land")
            delay()
            exp(Skill.Agility, 15.0)
            if (agilityStage == 7) {
                agilityStage = 0
                inc("barbarian_course_advanced_laps")
                exp(Skill.Agility, 615.0)
            }
        }
    }
}
