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
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

// TODO failing

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
    player.clear("face_entity")
    player.face(Direction.SOUTH)
    player.start("input_delay", 5)
    player.strongQueue("agility_rope_swing", 2) {
        player.setAnimation("rope_swing")
        target.animate("swing_rope")
        pause(1)
        player.exactMove(player.tile.copy(y = 3549), 60, Direction.SOUTH)
        pause(1)
        player.agilityStage(0)
        player.exp(Skill.Agility, 22.0)
    }
}

objectOperate("Walk-across", "barbarian_outpost_log_balance") {
    player.start("input_delay", 12)
    player.strongQueue("agility_log_balance") {
        onCancel = {
            player.tele(2551, 3546)
        }
        player.renderEmote = "rope_balance"
        player.message("You walk carefully across the slippery log...", ChatType.Filter)
        player.walkTo(Tile(2550, 3546), noCollision = true, noRun = true)
        pause(1)
        player.walkTo(Tile(2541, 3546), noCollision = true, noRun = true)
        pause(9)
        player.clearRenderEmote()
        player.agilityStage(1)
        player.exp(Skill.Agility, 13.7)
        player.message("... and make it safely to the other side.", ChatType.Filter)
    }
}

objectOperate("Climb-over", "barbarian_outpost_obstacle_net") {
    player.message("You climb the netting...", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("agility_netting", 2) {
        player.agilityStage(2)
        player.tele(2537, player.tile.y.coerceIn(3545, 3546), 1)
        player.exp(Skill.Agility, 8.2)
    }
}

objectOperate("Walk-across", "barbarian_outpost_balancing_ledge") {
    player.start("input_delay", 6)
    player.setAnimation("ledge_stand_right")
    player.strongQueue("agility_ledge_balance", 1) {
        onCancel = {
            player.tele(2536, 3547, 1)
        }
        player.renderEmote = "ledge_balance"
        player.message("You walk carefully across the slippery ledge...", ChatType.Filter)
        player.walkTo(Tile(2532, 3547, 1), noCollision = true, noRun = true)
        pause(5)
        player.face(Direction.WEST)
        player.setAnimation("ledge_stand_away_right")
        player.clearRenderEmote()
        player.agilityStage(3)
        player.exp(Skill.Agility, 22.0)
        player.message("... and make it safely to the other side.", ChatType.Filter)
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
            if (player.agilityStage == 4) {
                player.agilityStage = 0
                player.exp(Skill.Agility, 46.3)
                player.inc("barbarian_course_laps")
            }
        } else {
            player.agilityStage(4)
        }
        player.exp(Skill.Agility, 13.7)
    }
}