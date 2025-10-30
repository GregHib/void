package content.skill.agility.course

import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

class BarbarianOutpost : Script {

    init {
        objectOperate("Squeeze-through", "barbarian_outpost_entrance") { (target) ->
            if (!has(Skill.Agility, 35, message = true)) {
                return@objectOperate
            }
            agilityCourse("barbarian")
            val start = if (tile.y >= 3560) Tile(2552, 3561) else Tile(2552, 3558)
            if (tile != start) {
                walkToDelay(start)
                face(target)
                delay()
            }
            anim("climb_through_pipe")
            val end = if (tile.y >= 3560) 3558 else 3561
            exactMoveDelay(Tile(2552, end), 60, direction = if (tile.y >= 3560) Direction.SOUTH else Direction.NORTH)
        }

        objectOperate("Swing-on", "barbarian_outpost_rope_swing") { (target) ->
            walkToDelay(tile.copy(y = 3554))
            arriveDelay()
            clear("face_entity")
            face(Direction.SOUTH)
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 70) // 50% success at 35
            //  message("The rope swing is being used at the moment.", ChatType.Filter)
            anim("rope_swing")
            target.anim("swing_rope")
            delay()
            if (success) {
                exactMove(tile.copy(y = 3549), 60, Direction.SOUTH)
                delay()
                exp(Skill.Agility, 22.0)
                message("You skillfully swing across.", ChatType.Filter)
            } else {
                exactMoveDelay(tile.copy(y = 3550), 50, Direction.SOUTH)
                delay(1)
                tele(tile.copy(y = 9950))
                damage(50)
                delay(3)
                message("You slip and fall to the pit below.", ChatType.Filter)
                walkOverDelay(tile.copy(y = 9949))
            }
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(1)
            }
        }

        objectOperate("Walk-across", "barbarian_outpost_log_balance") {
            walkOverDelay(Tile(2550, 3546))
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 93) // 62.1% success rate at 35
            message("You walk carefully across the slippery log...", ChatType.Filter)
            renderEmote("rope_balance")
            if (success) {
                walkOverDelay(Tile(2541, 3546))
                clearRenderEmote()
                exp(Skill.Agility, 13.7)
                message("... and make it safely to the other side.", ChatType.Filter)
            } else {
                clear("face_entity")
                face(Direction.WEST)
                walkOverDelay(Tile(2545, 3546))
                anim("fall_off_log_left")
                delay()
                message("... but you lose your footing and fall into the water.", ChatType.Filter)
                tele(2545, 3545)
                renderEmote("tread_water")
                delay()
                walkOverDelay(Tile(2545, 3543))
                message("Something in the water bites you.", ChatType.Filter)
                clearRenderEmote()
                damage(random.nextInt(30, 52))
            }
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(2)
            }
        }

        objectOperate("Climb-over", "barbarian_outpost_obstacle_net") {
            message("You climb the netting...", ChatType.Filter)
            anim("climb_up")
            delay(2)
            agilityStage(3)
            tele(2537, tile.y.coerceIn(3545, 3546), 1)
            exp(Skill.Agility, 8.2)
        }

        objectOperate("Walk-across", "barbarian_outpost_balancing_ledge") {
            anim("ledge_stand_right")
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 93) // 62.1% success rate
            delay()
            renderEmote("ledge_balance")
            message("You put your foot on the ledge and try to edge across...", ChatType.Filter)
            if (success) {
                walkOverDelay(Tile(2532, 3547, 1))
                face(Direction.WEST)
                anim("ledge_stand_away_right")
                clearRenderEmote()
                exp(Skill.Agility, 22.0)
                message("You skillfully edge across the gap.", ChatType.Filter)
            } else {
                // https://youtu.be/bPFbuMnCx18?si=KJIVXuBftlZ9_Wth&t=47
                walkOverDelay(Tile(2534, 3547, 1))
                face(Direction.WEST)
                anim("fall_off_log_left")
                delay()
                tele(2534, 3546, 1)
                face(Direction.SOUTH)
                renderEmote("falling")
                delay()
                tele(2534, 3546, 0)
                clearRenderEmote()
                damage(50)
                delay()
                walkOverDelay(Tile(2534, 3545))
                // message("", ChatType.Filter) // TODO
            }
            // Skip stage so lap doesn't count at end
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(4)
            }
        }

        objectOperate("Climb-over", "barbarian_outpost_crumbling_wall") { (target) ->
            if (tile.x > target.tile.x) {
                message("You cannot climb that from this side.")
                return@objectOperate
            }
            if (tile.x == target.tile.x) {
                walkToDelay(target.tile.addX(-1))
            }
            message("You climb the low wall...", ChatType.Filter)
            anim("climb_over_wall")
            exactMove(target.tile.addX(1), 60, Direction.EAST)
            delay()
            if (target.tile.equals(2542, 3553)) {
                if (agilityStage == 5) {
                    agilityStage = 0
                    exp(Skill.Agility, 46.3)
                    inc("barbarian_course_laps")
                }
            } else {
                agilityStage(5)
            }
            exp(Skill.Agility, 13.7)
            delay()
        }
    }
}
