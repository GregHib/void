package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.setExactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Run-up", "barbarian_outpost_run_wall") {
    if (!player.has(Skill.Agility, 90, message = true)) {
        return@objectOperate
    }
    player.clear("face_entity")
    delay()
    player.face(Direction.NORTH)
    player.setAnimation("barbarian_wall_jump_climb")
    delay(7)
    player.setAnimation("barbarian_wall_jump")
    player.exactMove(Tile(2538, 3545, 2), 30, Direction.NORTH)
    delay(1)
    player.exp(Skill.Agility, 15.0)
    player.agilityStage(3)
}

objectOperate("Climb-up", "barbarian_outpost_climb_wall") {
    player.clear("face_entity")
    player.walkTo(Tile(2537, 3546, 2))
    player.face(Direction.WEST)
    delay()
    player.setAnimation("barbarian_wall_climb")
    delay()
    player.tele(2536, 3546, 3)
    player.setAnimation("barbarian_wall_stand_up")
    delay()
    player.exp(Skill.Agility, 15.0)
    player.agilityStage(4)
}

objectOperate("Fire", "barbarian_outpost_spring") {
    player.clear("face_entity")
    player.face(Direction.NORTH)
    delay(1)
    target.animate("barbarian_spring_fire")
    delay(1)
    player.tele(2533, 3547, 3)
    player.setAnimation("barbarian_spring_shoot")
    player.exactMove(Tile(2532, 3553, 3), 60, Direction.NORTH)
    target.animate("barbarian_spring_reset")
    delay(2)
    player.exp(Skill.Agility, 15.0)
    player.agilityStage(5)
}

objectOperate("Cross", "barbarian_outpost_balance_beam") {
    player.face(Direction.EAST)
    delay()
    player.setAnimation("circus_cartwheel")
    delay()
    player.exactMove(Tile(2536, 3553, 3), 45, Direction.EAST)
    player.renderEmote("beam_balance")
    delay()
    player.exp(Skill.Agility, 15.0)
    player.agilityStage(6)
}

objectOperate("Jump-over", "barbarian_outpost_gap") {
    player.clearRenderEmote()
    player.setAnimation("barbarian_gap_jump")
    delay()
    player.tele(2539, 3553, 2)
    player.setAnimation("barbarian_jump_land")
    delay()
    player.exp(Skill.Agility, 15.0)
    player.agilityStage(7)
}

objectOperate("Slide-down", "barbarian_outpost_roof") {
    player.setAnimation("barbarian_slide_start")
    player.exactMove(player.tile.copy(x = 2540), 30, Direction.EAST)
    player.setAnimation("barbarian_slide")
    player.setExactMove(player.tile.copy(x = 2543, level = 1), 90, Direction.EAST)
    delay()
    player.setAnimation("barbarian_slide")
    delay()
    player.setAnimation("barbarian_slide_jump")
    delay()
    player.tele(2544, player.tile.y, 0)
    player.setAnimation("barbarian_jump_land")
    delay()
    player.exp(Skill.Agility, 15.0)
    if (player.agilityStage == 7) {
        player.agilityStage = 0
        player.inc("barbarian_course_advanced_laps")
        player.exp(Skill.Agility, 615.0)
    }
}