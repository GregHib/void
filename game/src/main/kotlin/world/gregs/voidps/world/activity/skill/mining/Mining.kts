package world.gregs.voidps.world.activity.skill.mining

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.Math
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.skill.mining.rock.Rock
import kotlin.random.Random

val players: Players by inject()

val minPlayers = 0
val maxPlayers = 2000

/*
    When mining or prospecting (even with no pick)
        "There is currently no ore available in this rock."
 */
on<ObjectOption>({ option == "Mine" }) { player: Player ->
    if (obj.stringId.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@on
    }
    player.action(ActionType.Mining) {
        var first = true
        while (isActive && player.awaitDialogues()) {
            if (player.inventory.isFull()) {
                player.message("Your inventory is too full to hold any more ore.")
                break
            }

            val rock = Rock.get(obj)
            if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
                break
            }

            val pickaxe = Pickaxe.get(player)
            if (!Pickaxe.hasRequirements(player, pickaxe, true) || pickaxe == null) {
                break
            }

            player.setAnimation("${pickaxe.id}_swing_low")
            if (first) {
                player.message("You swing your pickaxe at the rock.")
                first = false
            }
            delay(4)
            if (success(player.levels.get(Skill.Mining), pickaxe, rock)) {
                player.experience.add(Skill.Mining, rock.xp)

                if (!addOre(player, rock) || deplete(rock, obj)) {
                    break
                }
            }
        }
    }
}

fun success(level: Int, pickaxe: Pickaxe, rock: Rock): Boolean {
//    val lowHatchetChance = pickaxe.calculateChance(rock.lowDifference)
//    val highHatchetChance = pickaxe.calculateChance(rock.highDifference)
//    val chance = rock.chance.first + lowHatchetChance..rock.chance.last + highHatchetChance
//    return Level.success(level, chance)
    return true
}

fun addOre(player: Player, rock: Rock): Boolean {
    val ore = rock.ore
    val added = player.inventory.add(ore.id)
    if (added) {
        player.message("You manage to mine some ${ore.id.replace("_", " ").toLowerCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}

fun deplete(rock: Rock, obj: GameObject): Boolean {
    val depleted = Random.nextDouble() <= 1.0//rock.depleteRate
    if (!depleted) {
        return false
    }
    val rockId = obj.def["rock", -1]
    if (rockId != -1) {
        val delay = getRegrowTickDelay(rock)
        obj.replace(rockId, ticks = delay)
//        areaSound("fell_tree", obj.tile, 5)
    }
    return true
}

/**
 * Returns regrow delay based on the type of tree and number of players online
 */
fun getRegrowTickDelay(rock: Rock): Int {
    val delay = 30..60//rock.respawnDelay
    return if (rock.level == 1) {
        Random.nextInt(delay.first, delay.last)// Regular tree's
    } else {
        Math.interpolate(players.count, delay.last, delay.first, minPlayers, maxPlayers)
    }
}

on<ObjectOption>({ option == "Prospect" }) { player: Player ->
    if (obj.stringId.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@on
    }
    player.action(ActionType.Prospecting) {
        player.message("You examine the rock for ores...")
        delay(4)
        val ore = Rock.get(obj)?.ore
        if (ore == null) {
            player.message("This rock contains no ore.")
        } else {
            player.message("This rock contains ${ore.id.replace("_", " ")}.")
        }
    }
}