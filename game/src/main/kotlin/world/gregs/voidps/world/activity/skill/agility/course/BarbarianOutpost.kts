package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.getPropertyOrNull
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.damage

objectOperate("Squeeze-through", "barbarian_outpost_entrance") {
    if (!player.has(Skill.Agility, 35, message = true)) {
        return@objectOperate
    }
    player.agilityCourse("barbarian")
    val start = if (player.tile.y >= 3560) Tile(2552, 3561) else Tile(2552, 3558)
    player.strongQueue("agility_pipe") {
        if (player.tile != start) {
            player.start("input_delay", 2)
            player.walkTo(start)
            pause()
            player.face(target)
            pause()
        }
        player.start("input_delay", 2)
        player.setAnimation("climb_through_pipe")
        val end = if (player.tile.y >= 3560) 3558 else 3561
        player.exactMove(Tile(2552, end), 60)
    }
}

objectOperate("Swing-on", "barbarian_outpost_rope_swing") {
    player.walkTo(player.tile.copy(y = 3554))
    player.clear("face_entity")
    player.face(Direction.SOUTH)
    val disable = getPropertyOrNull("disableAdvancedAgilityCourseFailure").toBoolean()
    val success = disable || Level.success(player.levels.get(Skill.Agility), 70) // 50% success at 35
//  player.message("The rope swing is being used at the moment.", ChatType.Filter)
    player.start("input_delay", if (success) 5 else 8)
    player.strongQueue("agility_rope_swing", 2) {
        player.setAnimation("rope_swing")
        target.animate("swing_rope")
        pause()
        if (success) {
            player.exactMove(player.tile.copy(y = 3549), 60, Direction.SOUTH)
            pause()
            player.exp(Skill.Agility, 22.0)
            player.message("You skillfully swing across.", ChatType.Filter)
        } else {
            player.exactMove(player.tile.copy(y = 3550), 50, Direction.SOUTH)
            pause(2)
            player.tele(player.tile.copy(y = 9950))
            player.damage(50)
            pause(3)
            player.walkTo(player.tile.copy(y = 9949), noCollision = true, noRun = true)
//            player.message("", ChatType.Filter) TODO
        }
        if (success || getPropertyOrNull("disableAdvancedAgilityCourseFailLapSkip").toBoolean()) {
            player.agilityStage(1)
        }
    }
}

objectOperate("Walk-across", "barbarian_outpost_log_balance") {
    player.start("input_delay", 12)
    val disable = getPropertyOrNull("disableAdvancedAgilityCourseFailure").toBoolean()
    val success = disable || Level.success(player.levels.get(Skill.Agility), 93) // 62.1% success rate at 35
    player.strongQueue("agility_log_balance") {
        onCancel = {
            player.tele(2551, 3546)
        }
        player.message("You walk carefully across the slippery log...", ChatType.Filter)
        player.renderEmote = "rope_balance"
        player.walkTo(Tile(2550, 3546), noCollision = true, noRun = true)
        pause(1)
        if (success) {
            player.walkTo(Tile(2541, 3546), noCollision = true, noRun = true)
            pause(9)
            player.clearRenderEmote()
            player.exp(Skill.Agility, 13.7)
            player.message("... and make it safely to the other side.", ChatType.Filter)
        } else {
            player.clear("face_entity")
            player.face(Direction.WEST)
            player.walkTo(Tile(2545, 3546), noCollision = true, noRun = true)
            pause(7)
            player.setAnimation("fall_off_log_left")
            pause(1)
            player.message("... but you lose your footing and fall into the water.", ChatType.Filter)
            player.tele(2545, 3545)
            player.renderEmote = "tread_water"
            pause(1)
            player.walkTo(Tile(2545, 3543), noCollision = true, noRun = true)
            pause(2)
            player.message("Something in the water bites you.", ChatType.Filter)
            player.clearRenderEmote()
            player.damage(random.nextInt(30, 52))
        }
        if (success || getPropertyOrNull("disableAdvancedAgilityCourseFailLapSkip").toBoolean()) {
            player.agilityStage(2)
        }
    }
}

objectOperate("Climb-over", "barbarian_outpost_obstacle_net") {
    player.message("You climb the netting...", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("agility_netting", 2) {
        player.agilityStage(3)
        player.tele(2537, player.tile.y.coerceIn(3545, 3546), 1)
        player.exp(Skill.Agility, 8.2)
    }
}

objectOperate("Walk-across", "barbarian_outpost_balancing_ledge") {
    player.start("input_delay", 6)
    player.setAnimation("ledge_stand_right")
    val disable = getPropertyOrNull("disableAdvancedAgilityCourseFailure").toBoolean()
    val success = disable || Level.success(player.levels.get(Skill.Agility), 93) // 62.1% success rate
    player.strongQueue("agility_ledge_balance", 1) {
        onCancel = {
            player.tele(2536, 3547, 1)
        }
        player.renderEmote = "ledge_balance"
        player.message("You put your foot on the ledge and try to edge across...", ChatType.Filter)
        if (success) {
            player.walkTo(Tile(2532, 3547, 1), noCollision = true, noRun = true)
            pause(5)
            player.face(Direction.WEST)
            player.setAnimation("ledge_stand_away_right")
            player.clearRenderEmote()
            player.exp(Skill.Agility, 22.0)
            player.message("You skillfully edge across the gap.", ChatType.Filter)
        } else {
            // https://youtu.be/bPFbuMnCx18?si=KJIVXuBftlZ9_Wth&t=47
            player.walkTo(Tile(2534, 3547, 1), noCollision = true, noRun = true)
            pause(2)
            player.face(Direction.WEST)
            player.setAnimation("fall_off_log_left")
            pause()
            player.tele(2534, 3546, 1)
            player.face(Direction.SOUTH)
            player.renderEmote = "falling"
            pause()
            player.tele(2534, 3546, 0)
            player.clearRenderEmote()
            player.damage(50)
            pause()
            player.walkTo(Tile(2534, 3545), noCollision = true, noRun = true)
    //            player.message("", ChatType.Filter) // TODO
            // Skip stage so lap doesn't count at end
        }
        if (success || getPropertyOrNull("disableAdvancedAgilityCourseFailLapSkip").toBoolean()) {
            player.agilityStage(4)
        }
    }
}

objectOperate("Climb-over", "barbarian_outpost_crumbling_wall") {
    if (player.tile.x > target.tile.x) {
        player.message("You cannot climb that from this side.")
        return@objectOperate
    }
    player.strongQueue("agility_wall_climb") {
        if (player.tile.x == target.tile.x) {
            player.walkTo(target.tile.addX(-1))
            pause(2)
        }
        player.start("input_delay", 2)
        player.message("You climb the low wall...", ChatType.Filter)
        player.setAnimation("climb_over_wall")
        player.exactMove(target.tile.addX(1), 60, Direction.EAST)
        pause(1)
        if (target.tile.equals(2542, 3553)) {
            if (player.agilityStage == 5) {
                player.agilityStage = 0
                player.exp(Skill.Agility, 46.3)
                player.inc("barbarian_course_laps")
            }
        } else {
            player.agilityStage(5)
        }
        player.exp(Skill.Agility, 13.7)
    }
}