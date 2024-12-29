package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

val npcs: NPCs by inject()

objectOperate("Climb-up", "gnome_tree_branch_advanced") {
    if (!player.has(Skill.Agility, 85)) {
        return@objectOperate
    }
    npcs.gnomeTrainer("Terrorbirds could climb faster than that!", Zone(9263413))
    player.message("You climb the tree...", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("branch", 2) {
        player.message("... to an even higher platform.", ChatType.Filter)
        player.gnomeStage(4)
        player.tele(player.tile.add(y = -1, level = 1))
        player.exp(Skill.Agility, 25.0)
    }
}

objectApproach("Run-across", "gnome_sign_post_advanced") {
    npcs.gnomeTrainer("Come on! I'd be over there by now.", Zone(13457717))
    player.approachRange(1)
    // Pausing for 2 ticks to ensure we're in the correct spot.
    // arriveDelay() wouldn't work as objectApproach is called before Movement.tick where "last_movement" is set
    pause(2)
    player.face(Direction.EAST)
    player.setAnimation("gnome_wall_run", override = true)
    player.start("input_delay", 4)
    player.strongQueue("wall-run", 1) {
        player.exactMove(Tile(2484, 3418, 3), 60, Direction.EAST)
        player.strongQueue("land", 2) {
            player.gnomeStage(5)
            player.exp(Skill.Agility, 25.0)
            player.clearAnimation()
        }
    }
}

objectApproach("Swing-to", "gnome_pole_advanced") {
    var tile = player.tile
    if (player.tile.x == 2484) {
        tile = Tile(2485, 3418, 3)
    }
    player.steps.clear()
    player.face(Direction.NORTH)
    player.start("input_delay", 14)
    player.strongQueue("run-up", 1) {
        player.setAnimation("gnome_run_up")
        player.exactMove(tile.copy(y = 3421), 60, Direction.NORTH)
        pause(2)
        player.setAnimation("gnome_jump")
        player.exactMove(tile.copy(y = 3425), 30, Direction.NORTH)
        pause(1)
        player.setAnimation("gnome_swing")
        pause(4)
        player.exactMove(tile.copy(y = 3429), 30, Direction.NORTH)
        pause(5)
        player.exactMove(tile.copy(y = 3432), 30, Direction.NORTH)
        delay(2)
        player.gnomeStage(6)
        player.exp(Skill.Agility, 25.0)
    }
}

objectOperate("Jump-over", "gnome_barrier_advanced") {
    player.setAnimation("gnome_jump_barrier")
    player.start("input_delay", 4)
    player.strongQueue("branch", 1) {
        player.exactMove(Tile(2485, 3434, 3), 30, Direction.NORTH)
        pause(2)
        player.tele(2485, 3436, 0)
        player.setAnimation("gnome_pipe_land")
        if (player.gnomeStage == 6) {
            player.gnomeStage = 0
            player.inc("gnome_course_advanced_laps")
            player.exp(Skill.Agility, 605.0)
        }
        player.exp(Skill.Agility, 25.0)
    }
}