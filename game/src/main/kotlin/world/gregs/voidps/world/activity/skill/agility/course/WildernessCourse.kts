package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
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
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.Action
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.obj.door.Door
import world.gregs.voidps.world.interact.entity.sound.playSound

val objects: GameObjects by inject()

objectOperate("Open", "wilderness_agility_door_closed") {
    if (!player.has(Skill.Agility, 52, message = true)) {
        // TODO proper message
        return@objectOperate
    }
    if (player.tile.y > 3916) {
        Door.enter(player, target)
        return@objectOperate
    }
    // Not sure if you can fail going up
//    val disable = Settings["agility.disableCourseFailure", false]
    val success = true//disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.strongQueue("course_enter") {
        onCancel = {
            player.tele(target.tile.addY(-1))
        }
        player.start("input_delay", if (success) 16 else 8)
        player.message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
        Door.enter(player, target)
        pause()
        player.renderEmote = "beam_balance"
//        if (!success) {
//            fallIntoPit()
//            return@strongQueue
//        }
        player.walkTo(Tile(2998, 3930), noCollision = true, noRun = true)
        pause(13)
        player.clearRenderEmote()
        val gateTile = Tile(2998, 3931)
        val gate = objects[gateTile, "wilderness_agility_gate_east_closed"]
        if (gate != null) {
            Door.enter(player, gate)
        } else {
            player.walkTo(gateTile, noCollision = true, noRun = true)
        }
        player.message("You skillfully balance across the ridge...", ChatType.Filter)
        player.exp(Skill.Agility, 15.0)
        player.agilityCourse("wilderness")
    }
}

objectOperate("Open", "wilderness_agility_gate_east_closed", "wilderness_agility_gate_west_closed") {
    if (player.tile.y < 3931) {
        Door.enter(player, target)
        return@objectOperate
    }
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.strongQueue("course_exit") {
        onCancel = {
            player.tele(target.tile.addY(1))
        }
        player.start("input_delay", if (success) 16 else 10)
        player.message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
        Door.enter(player, target)
        pause(if (target.id.endsWith("west_closed")) 2 else 1)
        player.renderEmote = "beam_balance"
        if (!success) {
            fallIntoPit()
            return@strongQueue
        }
        player.walkTo(Tile(2998, 3917), noCollision = true, noRun = true)
        pause(13)
        player.clearRenderEmote()
        val door = objects[Tile(2998, 3917), "wilderness_agility_door_closed"]
        if (door != null) {
            Door.enter(player, door)
        } else {
            player.walkTo(Tile(2998, 3916), noCollision = true, noRun = true)
        }
        player.message("You skillfully balance across the ridge...", ChatType.Filter)
        player.exp(Skill.Agility, 15.0)
    }
}

suspend fun Action.fallIntoPit() {
    player.walkTo(Tile(2998, 3924), noCollision = true, noRun = true)
    pause(7)
    player.clearRenderEmote()
    player.face(Direction.NORTH)
    player.setAnimation("rope_walk_fall_down")
    player.message("You lose your footing and fall into the wolf pit.", ChatType.Filter)
    pause()
    player.exactMove(Tile(3001, 3923), 25, Direction.SOUTH)
}

objectOperate("Squeeze-through", "wilderness_obstacle_pipe") {
    if (!target.tile.equals(3004, 3938)) {
        player.message("You can't enter the pipe from this side.")
        return@objectOperate
    }
    player.strongQueue("wilderness_pipe") {
        onCancel = {
            player.tele(target.tile.addY(-1))
        }
        if (player.tile.y == 3938) {
            player.start("input_delay", 2)
            player.walkTo(target.tile.addY(-1))
            pause(2)
        }
        player.start("input_delay", 8)
        player.setAnimation("climb_through_pipe", delay = 30)
        player.exactMove(Tile(3004, 3940), startDelay = 30, delay = 96, direction = Direction.NORTH)
        pause(4)
        player.tele(3004, 3947)
        pause()
        player.setAnimation("climb_through_pipe", delay = 30)
        player.exactMove(Tile(3004, 3950), startDelay = 30, delay = 96, direction = Direction.NORTH)
        pause(3)
        player.exp(Skill.Agility, 12.5)
        player.agilityStage(1)
    }
}

objectOperate("Swing-on", "wilderness_rope_swing") {
    player.walkTo(target.tile.copy(y = 3953))
    player.clear("face_entity")
    player.face(Direction.NORTH)
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.start("input_delay", if (success) 5 else 8)
    player.strongQueue("wilderness_rope_swing", 2) {
        player.setAnimation("rope_swing")
        target.animate("swing_rope")
        pause()
        if (success) {
            player.exactMove(player.tile.copy(y = 3958), 60, Direction.NORTH)
            pause()
            player.exp(Skill.Agility, 20.0)
            player.message("You skillfully swing across.", ChatType.Filter)
        } else {
            player.exactMove(player.tile.copy(y = 3957), 50, Direction.NORTH)
            pause(2)
            player.tele(3004, 10357)
            player.damage((player.levels.get(Skill.Constitution) * 0.15).toInt() + 10)
            player.message("You slip and fall to the pit below.", ChatType.Filter)
        }
        if (success || Settings["agility.disableFailLapSkip", false]) {
            player.agilityStage(2)
        }
    }
}

objectOperate("Cross", "wilderness_stepping_stone") {
    player.message("You carefully start crossing the stepping stones...", ChatType.Filter)
    player.start("input_delay", 12)
    player.strongQueue("agility_stepping_stones") {
        onCancel = {
            player.tele(3002, 3960)
        }
        for (i in 0..5) {
            player.setAnimation("stepping_stone_jump")
            player.playSound("jump")
            player.exactMove(target.tile.addX(-i), delay = 30, direction = Direction.WEST, startDelay = 15)
            pause(2)
            if (i == 2 && !Settings["agility.disableCourseFailure", false] && !Level.success(player.levels.get(Skill.Agility), 180..250)) {
                player.start("input_delay", 3)
                player.setAnimation("rope_walk_fall_down")
                player.face(Direction.WEST)
                player.clearRenderEmote()
                player.message("...You lose your footing and fall into the lava.", ChatType.Filter)
                pause(2)
                player.damage(player.levels.get(Skill.Constitution) / 5 + 10)
                player.tele(3002, 3963)
                if (Settings["agility.disableFailLapSkip", false]) {
                    player.agilityStage(3)
                }
                return@strongQueue
            }
        }
        player.message("...You safely cross to the other side.", ChatType.Filter)
        player.exp(Skill.Agility, 20.0)
        player.agilityStage(3)
    }
}

objectOperate("Walk-across", "wilderness_log_balance") {
    player.strongQueue("agility_log") {
        onCancel = {
            player.tele(3001, 3946)
        }
        player.message("You walk carefully across the slippery log...", ChatType.Filter)
        val disable = Settings["agility.disableCourseFailure", false]
        val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
        if (success) {
            player.start("input_delay", 10)
            player.walkTo(target.tile, noCollision = true, noRun = true)
            pause()
            player.renderEmote = "beam_balance"
            player.walkTo(Tile(2994, 3945), noCollision = true, noRun = true)
            pause(7)
            player.message("You skillfully edge across the gap.", type = ChatType.Filter)
            player.clearRenderEmote()
            pause()
            player.exp(Skill.Agility, 20.0)
            player.agilityStage(4)
        } else {
            player.start("input_delay", 9)
            player.walkTo(target.tile, noCollision = true, noRun = true)
            pause()
            player.renderEmote = "beam_balance"
            player.walkTo(Tile(2998, 3945), noCollision = true, noRun = true)
            pause(4)
            player.message("You slip and fall onto the spikes below.", type = ChatType.Filter)
            player.setAnimation("rope_walk_fall_down")
            player.face(Direction.NORTH)
            pause()
            player.tele(2998, 10346)
            player.clearRenderEmote()
            player.playSound("2h_stab")
            pause()
            player.walkTo(Tile(2998, 10345))
            pause()
            player.damage((player.levels.get(Skill.Constitution) * 0.15).toInt() + 10)
            player.playSound("male_hit_1", delay = 20)
        }
        if (success || Settings["agility.disableFailLapSkip", false]) {
            player.agilityStage(4)
        }
    }
}

objectOperate("Climb", "wilderness_agility_rocks") {
    player.start("input_delay", 5)
    player.strongQueue("agility_rocks") {
        onCancel = {
            player.tele(2993, 3937)
        }
        player.message("You walk carefully across the slippery log...", ChatType.Filter)
        player.renderEmote = "climbing"
        player.walkTo(player.tile.copy(y = 3933), noCollision = true, noRun = true)
        pause(4)
        player.clearRenderEmote()
        player.message("You reach the top.", type = ChatType.Filter)
        if (player.agilityStage == 4) {
            player.agilityStage = 0
            player.exp(Skill.Agility, 499.0)
            player.inc("wilderness_course_laps")
        }
    }
}