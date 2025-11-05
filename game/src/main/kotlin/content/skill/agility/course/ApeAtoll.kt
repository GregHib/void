package content.skill.agility.course

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class ApeAtoll : Script {

    init {
        objectOperate("Jump-to", "ape_atoll_stepping_stones") { (target) ->
            arriveDelay()
            face(target)
            delay()
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                !has(Skill.Agility, 48, message = true) -> return@objectOperate
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" -> {
                    agilityCourse("ape_atoll")
                    anim("ninja_monkey_jump")
                    delay()
                    sound("jump")
                    exactMoveDelay(Tile(2754, 2742), delay = 30)
                    delay(2)
                    anim("ninja_monkey_jump")
                    delay()
                    sound("jump")
                    exactMoveDelay(Tile(2753, 2742))
                    exp(Skill.Agility, 40.0)
                    agilityCourse("ape_atoll")
                    agilityStage(1)
                }
                // Human
                else -> {
                    message("The rock is covered in slime and you slip into the water...")
                    anim("stepping_stone_jump")
                    sound("jump")
                    exactMoveDelay(target.tile, direction = Direction.WEST)
                    anim("rope_walk_fall_down")
                    sound("stumble_loop", repeat = 10)
                    delay()
                    clearAnim()
                    renderEmote("swim")
                    walkOverDelay(target.tile.addY(1))
                    sound("jump")
                    areaGfx("big_splash", target.tile.addY(1), delay = 3)
                    sound("water_splash")
                    delay()
                    walkOverDelay(Tile(2757, 2748))
                    message("...you're not monkey enough to try this!")
                    damage()
                    clearRenderEmote()
                }
            }
        }

        objectOperate("Climb", "ape_atoll_tropical_tree") { (target) ->
            face(target)
            delay()
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    anim("ninja_monkey_climb_tree")
                    sound("climbing_loop", repeat = 3)
                    delay(3)
                    tele(2753, 2742, 2)
                    exp(Skill.Agility, 40.0)
                    agilityStage(2)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    message("You reach for the tree trunk and lose your footing...")
                    anim(if (monkey) "ninja_monkey_climb_tree_fail" else "climb_up")
                    if (monkey) {
                        sound("climbing_loop", repeat = 3)
                    } else {
                        sound("land_flatter", 20)
                    }
                    delay(if (monkey) 4 else 1)
                    if (monkey) {
                        message("You lose your grip on the tree and fall into the water.")
                        message("Something in the water bites you...")
                        areaGfx("big_splash", Tile(2753, 2743), 3)
                    } else {
                        message("...you're not monkey enough to try this!")
                        anim("human_death")
                        delay()
                        damageSound()
                    }
                    if (monkey) {
                        exactMoveDelay(Tile(2753, 2744), 5, Direction.SOUTH)
                    } else {
                        exactMoveDelay(Tile(2753, 2743), 30, Direction.SOUTH)
                    }
                    renderEmote(if (monkey) "ninja_monkey_swim" else "swim")
                    sound("water_splash")
                    sound("swim_stroke", repeat = 6)
                    returnToShore()
                }
            }
        }

        objectOperate("Swing Across", "ape_atoll_monkeybars") {
            face(Direction.WEST)
            delay()
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    walkOverDelay(Tile(2752, 2741, 2))
                    renderEmote("ninja_monkey_bars")
                    anim("ninja_monkey_bars_jump")
                    sound("monkeybars_on", repeat = 5)
                    sound("monkeybars_loop", repeat = 5)
                    walkOverDelay(Tile(2747, 2741, 2))
                    anim("ninja_monkey_jump_off")
                    sound("monkeybars_off", repeat = 5)
                    clearRenderEmote()
                    tele(2747, 2741, 0)
                    exp(Skill.Agility, 40.0)
                    agilityStage(3)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    anim(if (monkey) "ninja_monkey_bars_jump" else "jump_onto_monkey_bars")
                    renderEmote(if (monkey) "ninja_monkey_bars" else "monkey_bars")
                    sound("monkeybars_on", repeat = if (monkey) 5 else 1)
                    sound("monkeybars_loop", repeat = if (monkey) 4 else 1, delay = if (monkey) 120 else 30)
                    walkOverDelay(Tile(2750, 2741, 2))
                    anim(if (monkey) "ninja_monkey_bars_fail" else "rope_walk_fall_down")
                    delay()
                    if (monkey) {
                        message("Whoops! You lose your grip and fall into the river.")
                    } else {
                        message("Your hands slip from the rung...")
                        message("...you're not monkey enough to try this!")
                    }
                    tele(2750, 2742, 0)
                    clearAnim()
                    delay()
                    renderEmote(if (monkey) "ninja_monkey_swim" else "swim")
                    areaGfx("big_splash", Tile(2750, 2741), 3)
                    sound("watersplash")
                    sound("swim_stroke", repeat = 9)
                    returnToShore()
                }
            }
        }

        objectOperate("Climb-up", "ape_atoll_skull_slope") { (target) ->
            face(target)
            delay()
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    renderEmote("ninja_monkey_climb")
                    sound("climb_wall", repeat = 2)
                    walkTo(Tile(2744, 2741))
                    delay(2)
                    sound("climb_wall", repeat = 2)
                    walkTo(Tile(2743, 2741))
                    delay()
                    clearRenderEmote()
                    walkTo(Tile(2742, 2741))
                    delay()
                    exp(Skill.Agility, 60.0)
                    agilityStage(4)
                }
                // Failure
                weapon == "small_ninja_monkey_greegree" -> {
                    walkOverDelay(Tile(2746, 2741))
                    renderEmote("ninja_monkey_climb")
                    sound("climb_wall", repeat = 2)
                    walkOverDelay(Tile(2745, 2741))
                    message("You miss a hand hold...")
                    message("...and slide back down the slope.")
                    damage()
                    anim("ninja_monkey_fall")
                    sound("stumble_loop", repeat = 2)
                    exactMoveDelay(Tile(2747, 2741), 50, Direction.WEST)
                }
                // Human
                else -> {
                    renderEmote("climbing")
                    sound("climb_wall", repeat = 2)
                    walkOverDelay(Tile(2745, 2741))
                    message("The hand holds are too small to hold onto...")
                    anim("rocks_climb_up_fail")
                    sound("stumble_loop", repeat = 15)
                    exactMoveDelay(Tile(2750, 2741), 90, Direction.EAST)
                    sound("male_defend_3", delay = 20)
                    message("...you're not monkey enough to try this!")
                    clearAnim()
                    renderEmote("swim")
                    areaGfx("big_splash", target.tile, delay = 3)
                    sound("water_splash")
                    sound("swim_stroke", repeat = 9)
                    walkOverDelay(Tile(2750, 2742))
                    returnToShore()
                }
            }
        }

        objectOperate("Swing", "ape_atoll_rope_swing") { (target) ->
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    walkOverDelay(Tile(2751, 2731))
                    anim("ninja_monkey_swing")
                    target.anim("ape_atol_rope_swing")
                    exactMoveDelay(Tile(2756, 2731), 40, Direction.EAST)
                    sound("swing_across")
                    exp(Skill.Agility, 100.0)
                    agilityStage(5)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    message("You lose your grip on the vine!")
                    anim(if (monkey) "ninja_monkey_swing" else "fail_rope_swing")
                    target.anim("ape_atol_rope_swing")
                    sound("fall_splash")
                    exactMoveDelay(Tile(2754, 2731), 45, Direction.EAST)
                    areaGfx("big_splash", Tile(2754, 2731), 3)
                    delay()
                    if (monkey) {
                        message("...something in the water bites you.")
                    } else {
                        message("...you're not monkey enough to try this!")
                    }
                    renderEmote(if (monkey) "ninja_monkey_swim" else "drowning")
                    face(Direction.NORTH)
                    sound("water_splash")
                    delay()
                    sound("swim_stroke", 6)
                    exactMoveDelay(Tile(2754, 2736), 40, Direction.NORTH)
                    if (monkey) {
                        walkOverDelay(Tile(2753, 2739))
                        exactMoveDelay(Tile(2753, 2742), 40, Direction.NORTH)
                    } else {
                        walkOverDelay(Tile(2754, 2739))
                        exactMoveDelay(Tile(2754, 2741), 30, Direction.NORTH)
                        walkOverDelay(Tile(2755, 2742))
                    }
                    clearRenderEmote()
                    damage()
                }
            }
        }

        objectOperate("Climb-down", "ape_atoll_tropical_tree_rope") { (target) ->
            exactMoveDelay(Tile(2758, 2735), 25, Direction.NORTH_EAST)
            delay()
            val weapon = equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    renderEmote("ninja_monkey_swing")
                    tele(2759, 2736, 1)
                    sound("rope_climb", repeat = 30)
                    walkOverDelay(Tile(2770, 2747, 1))
                    anim("ninja_monkey_jump_off")
                    tele(2770, 2747, 0)
                    sound("land_flat")
                    delay()
                    clearRenderEmote()
                    exp(Skill.Agility, 100.0)
                    if (agilityStage == 5) {
                        agilityStage = 0
                        exp(Skill.Agility, 200.0)
                        inc("ape_atoll_course_laps")
                    }
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    message("You jump up to seize the vine...")
                    face(target)
                    anim(if (monkey) "ninja_monkey_bars_jump" else "jump_onto_monkey_bars")
                    sound("monkeybars_on")
                    delay()
                    renderEmote("monkey_bars")
                    sound("monkeybars_loop", repeat = 2)
                    walkOverDelay(Tile(2759, 2736))
                    walkOverDelay(Tile(2760, 2737))
                    message("...and lose your grip!")
                    anim(if (monkey) "ninja_monkey_bars_fail" else "rope_walk_fall_down")
                    sound("stumble_loop", repeat = 3)
                    exactMoveDelay(Tile(2764, 2737), startDelay = 10, delay = 30, direction = Direction.NORTH_EAST)
                    damage()
                    sound("land_flat")
                    clearRenderEmote()
                }
            }
        }
    }

    suspend fun Player.returnToShore() {
        walkOverDelay(Tile(2753, 2745))
        walkOverDelay(Tile(2754, 2745))
        walkOverDelay(Tile(2756, 2747))
        walkOverDelay(Tile(2756, 2748))
        walkOverDelay(Tile(2757, 2748))
        clearRenderEmote()
        damage()
    }

    fun Player.damage() {
        damage((levels.get(Skill.Constitution) / 100) * 10)
        damageSound()
    }

    fun Player.damageSound() {
        sound(
            if (male) {
                "male_defend_${random.nextInt(0, 3)}"
            } else {
                "female_defend_${random.nextInt(0, 1)}"
            },
            20,
        )
    }
}
