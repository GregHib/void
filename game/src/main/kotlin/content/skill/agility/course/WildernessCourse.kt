package content.skill.agility.course

import content.entity.combat.hit.damage
import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class WildernessCourse : Script {

    val objects: GameObjects by inject()

    init {
        objectOperate("Open", "wilderness_agility_door_closed") { (target) ->
            if (!has(Skill.Agility, 52, message = true)) {
                // TODO proper message
                return@objectOperate
            }
            if (tile.y > 3916) {
                enterDoor(target)
                clearRenderEmote()
                return@objectOperate
            }
            // Not sure if you can fail going up
            //    val disable = Settings["agility.disableCourseFailure", false]
            val success = true // disable || Level.success(levels.get(Skill.Agility), 200..250)
            message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
            enterDoor(target, delay = 1)
            renderEmote("beam_balance")
            //    if (!success) {
            //        fallIntoPit()
            //        return@strongQueue
            //    }
            walkOverDelay(Tile(2998, 3930))
            clearRenderEmote()
            val gateTile = Tile(2998, 3931)
            val gate = objects[gateTile, "wilderness_agility_gate_east_closed"]
            if (gate != null) {
                enterDoor(gate)
            } else {
                walkOverDelay(gateTile)
            }
            message("You skillfully balance across the ridge...", ChatType.Filter)
            exp(Skill.Agility, 15.0)
            agilityCourse("wilderness")
        }

        objectOperate("Open", "wilderness_agility_gate_east_closed,wilderness_agility_gate_west_closed") { (target) ->
            if (tile.y < 3931) {
                enterDoor(target, delay = 2)
                clearRenderEmote()
                return@objectOperate
            }
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 200..250)
            message("You go through the gate and try to edge over the ridge...", ChatType.Filter)
            walkToDelay(tile.copy(x = tile.x.coerceIn(2997, 2998)))
            enterDoor(target)
            renderEmote("beam_balance")
            if (!success) {
                fallIntoPit()
                return@objectOperate
            }
            walkOverDelay(Tile(2998, 3917))
            clearRenderEmote()
            val door = objects[Tile(2998, 3917), "wilderness_agility_door_closed"]
            if (door != null) {
                enterDoor(door, delay = 1)
            } else {
                walkOverDelay(Tile(2998, 3916))
            }
            message("You skillfully balance across the ridge...", ChatType.Filter)
            exp(Skill.Agility, 15.0)
        }

        objectOperate("Squeeze-through", "wilderness_obstacle_pipe") { (target) ->
            if (!target.tile.equals(3004, 3938)) {
                message("You can't enter the pipe from this side.")
                return@objectOperate
            }
            if (tile.y == 3938) {
                walkToDelay(target.tile.addY(-1))
            }
            anim("climb_through_pipe", delay = 30)
            exactMoveDelay(Tile(3004, 3940), startDelay = 30, delay = 96, direction = Direction.NORTH)
            tele(3004, 3947)
            delay()
            anim("climb_through_pipe", delay = 30)
            exactMoveDelay(Tile(3004, 3950), startDelay = 30, delay = 96, direction = Direction.NORTH)
            exp(Skill.Agility, 12.5)
            agilityStage(1)
        }

        objectOperate("Swing-on", "wilderness_rope_swing") { (target) ->
            walkToDelay(target.tile.copy(y = 3953))
            clear("face_entity")
            face(Direction.NORTH)
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 200..250)
            anim("rope_swing")
            target.anim("swing_rope")
            delay()
            if (success) {
                exactMoveDelay(tile.copy(y = 3958), 60, Direction.NORTH)
                exp(Skill.Agility, 20.0)
                message("You skillfully swing across.", ChatType.Filter)
            } else {
                exactMoveDelay(tile.copy(y = 3957), 50, Direction.NORTH)
                delay(1)
                tele(3004, 10357)
                damage((levels.get(Skill.Constitution) * 0.15).toInt() + 10)
                message("You slip and fall to the pit below.", ChatType.Filter)
            }
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(2)
            }
        }

        objectOperate("Cross", "wilderness_stepping_stone") { (target) ->
            message("You carefully start crossing the stepping stones...", ChatType.Filter)
            for (i in 0..5) {
                anim("stepping_stone_jump")
                sound("jump")
                exactMoveDelay(target.tile.addX(-i), delay = 30, direction = Direction.WEST, startDelay = 15)
                delay(1)
                if (i == 2 && !Settings["agility.disableCourseFailure", false] && !Level.success(levels.get(Skill.Agility), 180..250)) {
                    anim("rope_walk_fall_down")
                    face(Direction.WEST)
                    clearRenderEmote()
                    message("...You lose your footing and fall into the lava.", ChatType.Filter)
                    delay(2)
                    damage(levels.get(Skill.Constitution) / 5 + 10)
                    tele(3002, 3963)
                    if (Settings["agility.disableFailLapSkip", false]) {
                        agilityStage(3)
                    }
                    return@objectOperate
                }
            }
            message("...You safely cross to the other side.", ChatType.Filter)
            exp(Skill.Agility, 20.0)
            agilityStage(3)
        }

        objectOperate("Walk-across", "wilderness_log_balance") { (target) ->
            message("You walk carefully across the slippery log...", ChatType.Filter)
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 200..250)
            if (success) {
                walkOverDelay(target.tile)
                renderEmote("beam_balance")
                walkOverDelay(Tile(2994, 3945))
                message("You skillfully edge across the gap.", type = ChatType.Filter)
                clearRenderEmote()
                delay()
                exp(Skill.Agility, 20.0)
                agilityStage(4)
            } else {
                walkOverDelay(target.tile)
                renderEmote("beam_balance")
                walkOverDelay(Tile(2998, 3945))
                message("You slip and fall onto the spikes below.", type = ChatType.Filter)
                anim("rope_walk_fall_down")
                face(Direction.NORTH)
                delay()
                tele(2998, 10346)
                clearRenderEmote()
                sound("2h_stab")
                delay()
                walkOverDelay(Tile(2998, 10345))
                damage((levels.get(Skill.Constitution) * 0.15).toInt() + 10)
                sound("male_defend_1", delay = 20)
            }
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(4)
            }
        }

        objectOperate("Climb", "wilderness_agility_rocks") {
            message("You walk carefully across the slippery log...", ChatType.Filter)
            renderEmote("climbing")
            walkOverDelay(tile.copy(y = 3933))
            clearRenderEmote()
            message("You reach the top.", type = ChatType.Filter)
            if (agilityStage == 4) {
                agilityStage = 0
                exp(Skill.Agility, 499.0)
                inc("wilderness_course_laps")
            }
        }
    }

    suspend fun Player.fallIntoPit() {
        walkOverDelay(Tile(2998, 3924))
        clearRenderEmote()
        face(Direction.NORTH)
        anim("rope_walk_fall_down")
        message("You lose your footing and fall into the wolf pit.", ChatType.Filter)
        delay()
        exactMoveDelay(Tile(3001, 3923), 25, Direction.SOUTH)
    }
}
