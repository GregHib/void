package content.skill.agility.course

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
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
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

@Script
class ApeAtoll {

    init {
        objectOperate("Jump-to", "ape_atoll_stepping_stones") {
            player.face(target)
            delay()
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                !player.has(Skill.Agility, 48, message = true) -> return@objectOperate
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" -> {
                    player.agilityCourse("ape_atoll")
                    player.anim("ninja_monkey_jump")
                    delay()
                    player.sound("jump")
                    player.exactMoveDelay(Tile(2754, 2742), delay = 30)
                    delay(2)
                    player.anim("ninja_monkey_jump")
                    delay()
                    player.sound("jump")
                    player.exactMoveDelay(Tile(2753, 2742))
                    player.exp(Skill.Agility, 40.0)
                    player.agilityCourse("ape_atoll")
                    player.agilityStage(1)
                }
                // Human
                else -> {
                    player.message("The rock is covered in slime and you slip into the water...")
                    player.anim("stepping_stone_jump")
                    player.sound("jump")
                    player.exactMoveDelay(target.tile, direction = Direction.WEST)
                    player.anim("rope_walk_fall_down")
                    player.sound("stumble_loop", repeat = 10)
                    delay()
                    player.clearAnim()
                    player.renderEmote("swim")
                    player.walkOverDelay(target.tile.addY(1))
                    player.sound("jump")
                    areaGfx("big_splash", target.tile.addY(1), delay = 3)
                    player.sound("water_splash")
                    delay()
                    player.walkOverDelay(Tile(2757, 2748))
                    player.message("...you're not monkey enough to try this!")
                    damage()
                    player.clearRenderEmote()
                }
            }
        }

        objectOperate("Climb", "ape_atoll_tropical_tree") {
            player.face(target)
            delay()
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(player.levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    player.anim("ninja_monkey_climb_tree")
                    player.sound("climbing_loop", repeat = 3)
                    delay(3)
                    player.tele(2753, 2742, 2)
                    player.exp(Skill.Agility, 40.0)
                    player.agilityStage(2)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    player.message("You reach for the tree trunk and lose your footing...")
                    player.anim(if (monkey) "ninja_monkey_climb_tree_fail" else "climb_up")
                    if (monkey) {
                        player.sound("climbing_loop", repeat = 3)
                    } else {
                        player.sound("land_flatter", 20)
                    }
                    delay(if (monkey) 4 else 1)
                    if (monkey) {
                        player.message("You lose your grip on the tree and fall into the water.")
                        player.message("Something in the water bites you...")
                        areaGfx("big_splash", Tile(2753, 2743), 3)
                    } else {
                        player.message("...you're not monkey enough to try this!")
                        player.anim("human_death")
                        delay()
                        damageSound()
                    }
                    if (monkey) {
                        player.exactMoveDelay(Tile(2753, 2744), 5, Direction.SOUTH)
                    } else {
                        player.exactMoveDelay(Tile(2753, 2743), 30, Direction.SOUTH)
                    }
                    player.renderEmote(if (monkey) "ninja_monkey_swim" else "swim")
                    player.sound("water_splash")
                    player.sound("swim_stroke", repeat = 6)
                    returnToShore()
                }
            }
        }

        objectOperate("Swing Across", "ape_atoll_monkeybars") {
            player.face(Direction.WEST)
            delay()
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(player.levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    player.walkOverDelay(Tile(2752, 2741, 2))
                    player.renderEmote("ninja_monkey_bars")
                    player.anim("ninja_monkey_bars_jump")
                    player.sound("monkeybars_on", repeat = 5)
                    player.sound("monkeybars_loop", repeat = 5)
                    player.walkOverDelay(Tile(2747, 2741, 2))
                    player.anim("ninja_monkey_jump_off")
                    player.sound("monkeybars_off", repeat = 5)
                    player.clearRenderEmote()
                    player.tele(2747, 2741, 0)
                    player.exp(Skill.Agility, 40.0)
                    player.agilityStage(3)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    player.anim(if (monkey) "ninja_monkey_bars_jump" else "jump_onto_monkey_bars")
                    player.renderEmote(if (monkey) "ninja_monkey_bars" else "monkey_bars")
                    player.sound("monkeybars_on", repeat = if (monkey) 5 else 1)
                    player.sound("monkeybars_loop", repeat = if (monkey) 4 else 1, delay = if (monkey) 120 else 30)
                    player.walkOverDelay(Tile(2750, 2741, 2))
                    player.anim(if (monkey) "ninja_monkey_bars_fail" else "rope_walk_fall_down")
                    delay()
                    if (monkey) {
                        player.message("Whoops! You lose your grip and fall into the river.")
                    } else {
                        player.message("Your hands slip from the rung...")
                        player.message("...you're not monkey enough to try this!")
                    }
                    player.tele(2750, 2742, 0)
                    player.clearAnim()
                    delay()
                    player.renderEmote(if (monkey) "ninja_monkey_swim" else "swim")
                    areaGfx("big_splash", Tile(2750, 2741), 3)
                    player.sound("watersplash")
                    player.sound("swim_stroke", repeat = 9)
                    returnToShore()
                }
            }
        }

        objectOperate("Climb-up", "ape_atoll_skull_slope") {
            player.face(target)
            delay()
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(player.levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    player.renderEmote("ninja_monkey_climb")
                    player.sound("climb_wall", repeat = 2)
                    player.walkTo(Tile(2744, 2741))
                    delay(2)
                    player.sound("climb_wall", repeat = 2)
                    player.walkTo(Tile(2743, 2741))
                    delay()
                    player.clearRenderEmote()
                    player.walkTo(Tile(2742, 2741))
                    delay()
                    player.exp(Skill.Agility, 60.0)
                    player.agilityStage(4)
                }
                // Failure
                weapon == "small_ninja_monkey_greegree" -> {
                    player.walkOverDelay(Tile(2746, 2741))
                    player.renderEmote("ninja_monkey_climb")
                    player.sound("climb_wall", repeat = 2)
                    player.walkOverDelay(Tile(2745, 2741))
                    player.message("You miss a hand hold...")
                    player.message("...and slide back down the slope.")
                    damage()
                    player.anim("ninja_monkey_fall")
                    player.sound("stumble_loop", repeat = 2)
                    player.exactMoveDelay(Tile(2747, 2741), 50, Direction.WEST)
                }
                // Human
                else -> {
                    player.renderEmote("climbing")
                    player.sound("climb_wall", repeat = 2)
                    player.walkOverDelay(Tile(2745, 2741))
                    player.message("The hand holds are too small to hold onto...")
                    player.anim("rocks_climb_up_fail")
                    player.sound("stumble_loop", repeat = 15)
                    player.exactMoveDelay(Tile(2750, 2741), 90, Direction.EAST)
                    player.sound("male_defend_3", delay = 20)
                    player.message("...you're not monkey enough to try this!")
                    player.clearAnim()
                    player.renderEmote("swim")
                    areaGfx("big_splash", target.tile, delay = 3)
                    player.sound("water_splash")
                    player.sound("swim_stroke", repeat = 9)
                    player.walkOverDelay(Tile(2750, 2742))
                    returnToShore()
                }
            }
        }

        objectOperate("Swing", "ape_atoll_rope_swing") {
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(player.levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    player.walkOverDelay(Tile(2751, 2731))
                    player.anim("ninja_monkey_swing")
                    target.anim("ape_atol_rope_swing")
                    player.exactMoveDelay(Tile(2756, 2731), 40, Direction.EAST)
                    player.sound("swing_across")
                    player.exp(Skill.Agility, 100.0)
                    player.agilityStage(5)
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    player.message("You lose your grip on the vine!")
                    player.anim(if (monkey) "ninja_monkey_swing" else "fail_rope_swing")
                    target.anim("ape_atol_rope_swing")
                    player.sound("fall_splash")
                    player.exactMoveDelay(Tile(2754, 2731), 45, Direction.EAST)
                    areaGfx("big_splash", Tile(2754, 2731), 3)
                    delay()
                    if (monkey) {
                        player.message("...something in the water bites you.")
                    } else {
                        player.message("...you're not monkey enough to try this!")
                    }
                    player.renderEmote(if (monkey) "ninja_monkey_swim" else "drowning")
                    player.face(Direction.NORTH)
                    player.sound("water_splash")
                    delay()
                    player.sound("swim_stroke", 6)
                    player.exactMoveDelay(Tile(2754, 2736), 40, Direction.NORTH)
                    if (monkey) {
                        player.walkOverDelay(Tile(2753, 2739))
                        player.exactMoveDelay(Tile(2753, 2742), 40, Direction.NORTH)
                    } else {
                        player.walkOverDelay(Tile(2754, 2739))
                        player.exactMoveDelay(Tile(2754, 2741), 30, Direction.NORTH)
                        player.walkOverDelay(Tile(2755, 2742))
                    }
                    player.clearRenderEmote()
                    damage()
                }
            }
        }

        objectOperate("Climb-down", "ape_atoll_tropical_tree_rope") {
            player.exactMoveDelay(Tile(2758, 2735), 25, Direction.NORTH_EAST)
            delay()
            val weapon = player.equipped(EquipSlot.Weapon).id
            when {
                weapon.endsWith("_greegree") && weapon != "small_ninja_monkey_greegree" -> {
                    player.message("Only the stealthiest and most agile monkey can use this!")
                }
                // Success
                weapon == "small_ninja_monkey_greegree" && (Level.success(player.levels.get(Skill.Agility), 70) || Settings["agility.disableCourseFailure", false]) -> {
                    player.renderEmote("ninja_monkey_swing")
                    player.tele(2759, 2736, 1)
                    player.sound("rope_climb", repeat = 30)
                    player.walkOverDelay(Tile(2770, 2747, 1))
                    player.anim("ninja_monkey_jump_off")
                    player.tele(2770, 2747, 0)
                    player.sound("land_flat")
                    delay()
                    player.clearRenderEmote()
                    player.exp(Skill.Agility, 100.0)
                    println("Stage ${player["agility_course", "unknown"]} ${player.agilityStage}")
                    if (player.agilityStage == 5) {
                        player.agilityStage = 0
                        player.exp(Skill.Agility, 200.0)
                        player.inc("ape_atoll_course_laps")
                    }
                }
                // Failure
                else -> {
                    val monkey = weapon == "small_ninja_monkey_greegree"
                    player.message("You jump up to seize the vine...")
                    player.face(target)
                    player.anim(if (monkey) "ninja_monkey_bars_jump" else "jump_onto_monkey_bars")
                    player.sound("monkeybars_on")
                    delay()
                    player.renderEmote("monkey_bars")
                    player.sound("monkeybars_loop", repeat = 2)
                    player.walkOverDelay(Tile(2759, 2736))
                    player.walkOverDelay(Tile(2760, 2737))
                    player.message("...and lose your grip!")
                    player.anim(if (monkey) "ninja_monkey_bars_fail" else "rope_walk_fall_down")
                    player.sound("stumble_loop", repeat = 3)
                    player.exactMoveDelay(Tile(2764, 2737), startDelay = 10, delay = 30, direction = Direction.NORTH_EAST)
                    damage()
                    player.sound("land_flat")
                    player.clearRenderEmote()
                }
            }
        }
    }

    suspend fun Interaction<Player>.returnToShore() {
        player.walkOverDelay(Tile(2753, 2745))
        player.walkOverDelay(Tile(2754, 2745))
        player.walkOverDelay(Tile(2756, 2747))
        player.walkOverDelay(Tile(2756, 2748))
        player.walkOverDelay(Tile(2757, 2748))
        player.clearRenderEmote()
        damage()
    }

    fun Interaction<Player>.damage() {
        player.damage((player.levels.get(Skill.Constitution) / 100) * 10)
        damageSound()
    }

    fun Interaction<Player>.damageSound() {
        player.sound(
            if (player.male) {
                "male_defend_${random.nextInt(0, 3)}"
            } else {
                "female_defend_${random.nextInt(0, 1)}"
            },
            20,
        )
    }
}
