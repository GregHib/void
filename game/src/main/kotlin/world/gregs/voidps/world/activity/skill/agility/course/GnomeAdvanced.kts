package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.world.interact.dialogue.HappyOld
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.combat.hit.damage

val npcs: NPCs by inject()

objectOperate("Climb-up", "gnome_tree_branch_advanced") {
    if (!player.has(Skill.Agility, 85, message = true)) {
        npc<HappyOld>("gnome_trainer", "Sorry mate, you're not experienced enough to try that route. I suggest you carry on over the balancing rope instead.")
        return@objectOperate
    }
    npcs.gnomeTrainer("Terrorbirds could climb faster than that!", Zone(9263413))
    player.message("You climb the tree...", ChatType.Filter)
    player.anim("climb_up")
    delay(2)
    player.message("... to an even higher platform.", ChatType.Filter)
    player.agilityStage(4)
    player.tele(player.tile.add(y = -1, level = 1))
    player.exp(Skill.Agility, 25.0)
}

objectApproach("Run-across", "gnome_sign_post_advanced") {
    npcs.gnomeTrainer("Come on! I'd be over there by now.", Zone(13457717))
    approachRange(1)
    arriveDelay()
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), -8..286) // failure rate 4.68-1.17% from 85-88
    player.face(Direction.EAST)
    player.anim("gnome_wall_${if (success) "run" else "fail"}")
    delay(1)
    if (!success) {
        player.exactMoveDelay(Tile(2480, 3418, 3), 30, Direction.EAST)
        delay(5)
    }
    player.exactMoveDelay(Tile(2484, 3418, 3), if (success) 60 else 210, Direction.EAST)
    if (success) {
        player.exp(Skill.Agility, 25.0)
    } else {
        delay(3)
        player.anim("gnome_wall_stand")
        delay()
        player.damage((player.levels.get(Skill.Constitution) - 10).coerceAtMost(65))
    }
    // Skip stage so lap doesn't count at end
    if (success || Settings["agility.disableFailLapSkip", false]) {
        player.agilityStage(5)
    }
    player.clearAnimation()
}

objectApproach("Swing-to", "gnome_pole_advanced") {
    var tile = player.tile
    if (player.tile.x == 2484) {
        tile = Tile(2485, 3418, 3)
    }
    player.steps.clear()
    player.face(Direction.NORTH)
    delay()
    player.anim("gnome_run_up")
    player.exactMoveDelay(tile.copy(y = 3421), 60, Direction.NORTH)
    player.anim("gnome_jump")
    player.exactMoveDelay(tile.copy(y = 3425), 30, Direction.NORTH)
    player.anim("gnome_swing")
    delay(4)
    player.exactMoveDelay(tile.copy(y = 3429), 30, Direction.NORTH)
    delay(4)
    player.exactMoveDelay(tile.copy(y = 3432), 30, Direction.NORTH)
    delay(1)
    player.agilityStage(6)
    player.exp(Skill.Agility, 25.0)
}

objectOperate("Jump-over", "gnome_barrier_advanced") {
    player.anim("gnome_jump_barrier")
    delay()
    player.exactMoveDelay(Tile(2485, 3434, 3), 30, Direction.NORTH)
    delay(1)
    player.tele(2485, 3436, 0)
    player.anim("gnome_pipe_land")
    if (player.agilityStage == 6) {
        player.agilityStage = 0
        player.inc("gnome_course_advanced_laps")
        player.exp(Skill.Agility, 605.0)
    }
    player.exp(Skill.Agility, 25.0)
}