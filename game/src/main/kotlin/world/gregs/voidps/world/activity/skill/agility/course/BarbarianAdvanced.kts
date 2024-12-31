package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Run-up", "barbarian_outpost_run_wall") {
    if (!player.has(Skill.Agility, 90, message = true)) {
        return@objectOperate
    }
    player.clear("face_entity")
    pause()
    player.face(Direction.NORTH)
    player.setAnimation("barbarian_wall_jump_climb")
    player.start("input_delay", 10)
    player.strongQueue("agility_wall", 7) {
        player.exactMove(Tile(2538, 3545, 2), 30, Direction.NORTH)
        player.setAnimation("barbarian_wall_jump")
        pause(2)
        player.exp(Skill.Agility, 15.0)
        player.agilityStage(3)
    }
}

objectOperate("Climb-up", "barbarian_outpost_climb_wall") {
    player.setAnimation("barbarian_wall_climb")
    player.start("input_delay", 3)
    player.strongQueue("agility_wall", 2) {
        player.tele(2536, 3546, 3)
        player.setAnimation("barbarian_wall_stand_up")
        pause(1)
        player.exp(Skill.Agility, 15.0)
        player.agilityStage(4)
    }
}

objectOperate("Fire", "barbarian_outpost_spring") {
    player.clear("face_entity")
    player.face(Direction.NORTH)
    player.start("input_delay", 6)
    player.strongQueue("agility_spring", 1) {
        target.animate("barbarian_spring_fire")
        pause(1)
        player.tele(2533, 3547, 3)
        player.exactMove(Tile(2532, 3553, 3), 60, Direction.NORTH)
        player.setAnimation("barbarian_spring_shoot")
        pause(1)
        target.animate("barbarian_spring_reset")
        pause(3)
        player.exp(Skill.Agility, 15.0)
        player.agilityStage(5)
    }
}

objectOperate("Cross", "barbarian_outpost_balance_beam") {
    player.face(Direction.EAST)
    player.start("input_delay", 6)
    player.strongQueue("agility_beam", 1) {
        onCancel = {
            player.tele(2533, 3553, 3)
            player.clearRenderEmote()
        }
        player.setAnimation("circus_cartwheel")
        pause()
        player.exactMove(Tile(2536, 3553, 3), 45, Direction.EAST)
        pause()
        player.renderEmote = "beam_balance"
        pause()
        player.exp(Skill.Agility, 15.0)
        player.agilityStage(6)
    }
}

objectOperate("Jump-over", "barbarian_outpost_gap") {
    player.clearRenderEmote()
    player.setAnimation("barbarian_gap_jump")
    player.start("input_delay", 2)
    player.strongQueue("agility_gap", 1) {
        player.tele(2539, 3553, 2)
        player.setAnimation("barbarian_jump_land")
        pause()
        player.exp(Skill.Agility, 15.0)
        player.agilityStage(7)
    }
}

objectOperate("Slide-down", "barbarian_outpost_roof") {
    player.start("input_delay", 5)
    player.strongQueue("agility_gap") {
        onCancel = {
            player.tele(2538, 3553, 2)
        }
        player.exactMove(player.tile.copy(x = 2540), 30, Direction.EAST)
        player.setAnimation("barbarian_slide_start")
        pause()
        player.setAnimation("barbarian_slide")
        player.exactMove(player.tile.copy(x = 2543, level = 1), 90, Direction.EAST)
        pause()
        player.setAnimation("barbarian_slide")
        pause()
        player.setAnimation("barbarian_slide_jump")
        pause()
        player.tele(2544, player.tile.y, 0)
        player.setAnimation("barbarian_jump_land")
        player.exp(Skill.Agility, 15.0)
        if (player.agilityStage == 7) {
            player.agilityStage = 0
            player.inc("barbarian_course_advanced_laps")
            player.exp(Skill.Agility, 615.0)
        }
    }
}