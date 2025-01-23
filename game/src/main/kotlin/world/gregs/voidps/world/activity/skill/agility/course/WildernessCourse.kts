package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkOver
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.obj.door.enterDoor
import world.gregs.voidps.world.interact.entity.sound.playSound

val objects: GameObjects by inject()

objectOperate("Open", "wilderness_agility_door_closed") {
    if (!player.has(Skill.Agility, 52, message = true)) {
        // TODO proper message
        return@objectOperate
    }
    if (player.tile.y > 3916) {
        enterDoor(target)
        player.clearRenderEmote()
        return@objectOperate
    }
    // Not sure if you can fail going up
//    val disable = Settings["agility.disableCourseFailure", false]
    val success = true//disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
    enterDoor(target, delay = 1)
    player.renderEmote("beam_balance")
//    if (!success) {
//        fallIntoPit()
//        return@strongQueue
//    }
    player.walkOver(Tile(2998, 3930))
    player.clearRenderEmote()
    val gateTile = Tile(2998, 3931)
    val gate = objects[gateTile, "wilderness_agility_gate_east_closed"]
    if (gate != null) {
        enterDoor(gate)
    } else {
        player.walkOver(gateTile)
    }
    player.message("You skillfully balance across the ridge...", ChatType.Filter)
    player.exp(Skill.Agility, 15.0)
    player.agilityCourse("wilderness")
}

objectOperate("Open", "wilderness_agility_gate_east_closed", "wilderness_agility_gate_west_closed") {
    if (player.tile.y < 3931) {
        enterDoor(target, delay = 2)
        player.clearRenderEmote()
        return@objectOperate
    }
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
    player.walkTo(player.tile.copy(x = player.tile.x.coerceIn(2997, 2998)))
    enterDoor(target)
    player.renderEmote("beam_balance")
    if (!success) {
        fallIntoPit()
        return@objectOperate
    }
    player.walkOver(Tile(2998, 3917))
    player.clearRenderEmote()
    val door = objects[Tile(2998, 3917), "wilderness_agility_door_closed"]
    if (door != null) {
        enterDoor(door, delay = 1)
    } else {
        player.walkOver(Tile(2998, 3916))
    }
    player.message("You skillfully balance across the ridge...", ChatType.Filter)
    player.exp(Skill.Agility, 15.0)
}

suspend fun SuspendableContext<Player>.fallIntoPit() {
    player.walkOver(Tile(2998, 3924))
    player.clearRenderEmote()
    player.face(Direction.NORTH)
    player.setAnimation("rope_walk_fall_down")
    player.message("You lose your footing and fall into the wolf pit.", ChatType.Filter)
    delay()
    player.exactMoveDelay(Tile(3001, 3923), 25, Direction.SOUTH)
}

objectOperate("Squeeze-through", "wilderness_obstacle_pipe") {
    if (!target.tile.equals(3004, 3938)) {
        player.message("You can't enter the pipe from this side.")
        return@objectOperate
    }
    if (player.tile.y == 3938) {
        player.walkTo(target.tile.addY(-1))
    }
    player.setAnimation("climb_through_pipe", delay = 30)
    player.exactMoveDelay(Tile(3004, 3940), startDelay = 30, delay = 96, direction = Direction.NORTH)
    player.tele(3004, 3947)
    delay()
    player.setAnimation("climb_through_pipe", delay = 30)
    player.exactMoveDelay(Tile(3004, 3950), startDelay = 30, delay = 96, direction = Direction.NORTH)
    player.exp(Skill.Agility, 12.5)
    player.agilityStage(1)
}

objectOperate("Swing-on", "wilderness_rope_swing") {
    player.walkTo(target.tile.copy(y = 3953))
    player.clear("face_entity")
    player.face(Direction.NORTH)
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    player.setAnimation("rope_swing")
    target.animate("swing_rope")
    delay()
    if (success) {
        player.exactMoveDelay(player.tile.copy(y = 3958), 60, Direction.NORTH)
        player.exp(Skill.Agility, 20.0)
        player.message("You skillfully swing across.", ChatType.Filter)
    } else {
        player.exactMoveDelay(player.tile.copy(y = 3957), 50, Direction.NORTH)
        delay(1)
        player.tele(3004, 10357)
        player.damage((player.levels.get(Skill.Constitution) * 0.15).toInt() + 10)
        player.message("You slip and fall to the pit below.", ChatType.Filter)
    }
    if (success || Settings["agility.disableFailLapSkip", false]) {
        player.agilityStage(2)
    }
}

objectOperate("Cross", "wilderness_stepping_stone") {
    player.message("You carefully start crossing the stepping stones...", ChatType.Filter)
    for (i in 0..5) {
        player.setAnimation("stepping_stone_jump")
        player.playSound("jump")
        player.exactMoveDelay(target.tile.addX(-i), delay = 30, direction = Direction.WEST, startDelay = 15)
        delay(1)
        if (i == 2 && !Settings["agility.disableCourseFailure", false] && !Level.success(player.levels.get(Skill.Agility), 180..250)) {
            player.setAnimation("rope_walk_fall_down")
            player.face(Direction.WEST)
            player.clearRenderEmote()
            player.message("...You lose your footing and fall into the lava.", ChatType.Filter)
            delay(2)
            player.damage(player.levels.get(Skill.Constitution) / 5 + 10)
            player.tele(3002, 3963)
            if (Settings["agility.disableFailLapSkip", false]) {
                player.agilityStage(3)
            }
            return@objectOperate
        }
    }
    player.message("...You safely cross to the other side.", ChatType.Filter)
    player.exp(Skill.Agility, 20.0)
    player.agilityStage(3)
}

objectOperate("Walk-across", "wilderness_log_balance") {
    player.message("You walk carefully across the slippery log...", ChatType.Filter)
    val disable = Settings["agility.disableCourseFailure", false]
    val success = disable || Level.success(player.levels.get(Skill.Agility), 200..250)
    if (success) {
        player.walkOver(target.tile)
        player.renderEmote("beam_balance")
        player.walkOver(Tile(2994, 3945))
        player.message("You skillfully edge across the gap.", type = ChatType.Filter)
        player.clearRenderEmote()
        delay()
        player.exp(Skill.Agility, 20.0)
        player.agilityStage(4)
    } else {
        player.walkOver(target.tile)
        player.renderEmote("beam_balance")
        player.walkOver(Tile(2998, 3945))
        player.message("You slip and fall onto the spikes below.", type = ChatType.Filter)
        player.setAnimation("rope_walk_fall_down")
        player.face(Direction.NORTH)
        delay()
        player.tele(2998, 10346)
        player.clearRenderEmote()
        player.playSound("2h_stab")
        delay()
        player.walkOver(Tile(2998, 10345))
        player.damage((player.levels.get(Skill.Constitution) * 0.15).toInt() + 10)
        player.playSound("male_hit_1", delay = 20)
    }
    if (success || Settings["agility.disableFailLapSkip", false]) {
        player.agilityStage(4)
    }
}

objectOperate("Climb", "wilderness_agility_rocks") {
    player.message("You walk carefully across the slippery log...", ChatType.Filter)
    player.renderEmote("climbing")
    player.walkOver(player.tile.copy(y = 3933))
    player.clearRenderEmote()
    player.message("You reach the top.", type = ChatType.Filter)
    if (player.agilityStage == 4) {
        player.agilityStage = 0
        player.exp(Skill.Agility, 499.0)
        player.inc("wilderness_course_laps")
    }
}