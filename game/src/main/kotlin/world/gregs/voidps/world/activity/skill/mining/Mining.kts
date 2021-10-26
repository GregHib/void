package world.gregs.voidps.world.activity.skill.mining

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.world.activity.skill.mining.ore.Ore
import world.gregs.voidps.world.activity.skill.mining.rock.Rock
import kotlin.random.Random

on<ObjectClick>({ option == "Mine" }) { player: Player ->
    cancel = player.hasEffect("skilling_delay")
}

on<ObjectOption>({ option == "Mine" }) { player: Player ->
    if (obj.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@on
    }
    player.action(ActionType.Mining) {
        try {
            var first = true
            mining@ while (isActive && player.awaitDialogues()) {
                if (player.inventory.isFull()) {
                    player.message("Your inventory is too full to hold any more ore.")
                    break
                }

                val rock = Rock.get(player, obj)
                if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
                    break
                }

                val pickaxe = Pickaxe.get(player)
                if (!Pickaxe.hasRequirements(player, pickaxe, true) || pickaxe == null) {
                    break
                }

                val delay = if (pickaxe == Pickaxe.DragonPickaxe && Random.nextInt(6) == 0) 2 else pickaxe.delay
                if (first) {
                    player.message("You swing your pickaxe at the rock.", ChatType.GameFilter)
                    player.start("skilling_delay", delay)
                    first = false
                }
                player.face(obj)
                player.setAnimation("${pickaxe.id}_swing_low")
                delay(delay)
                for (ore in rock.ores) {
                    if (success(player.levels.get(Skill.Mining), ore.chance)) {
                        player.experience.add(Skill.Mining, ore.xp)

                        if (!addOre(player, ore) || deplete(rock, obj)) {
                            break@mining
                        }
                    }
                }
            }
        } finally {
            player.clearAnimation()
        }
    }
}

fun addOre(player: Player, ore: Ore): Boolean {
    val added = player.inventory.add(ore.id)
    if (added) {
        player.message("You manage to mine some ${ore.id.toTitleCase().toLowerCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}

fun deplete(rock: Rock, obj: GameObject): Boolean {
    val depleted = obj.id.replace(rock.id, "depleted")
    if (rock.respawnDelay >= 0) {
        obj.replace(depleted, ticks = rock.respawnDelay)
        return true
    }
    return false
}

on<ObjectOption>({ option == "Prospect" }) { player: Player ->
    if (obj.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@on
    }
    player.action(ActionType.Prospecting) {
        withContext(NonCancellable) {
            player.message("You examine the rock for ores...")
            delay(4)
            val ore = Rock.get(player, obj)?.ores?.firstOrNull()
            if (ore == null) {
                player.message("This rock contains no ore.")
            } else {
                player.message("This rock contains ${ore.id.toTitleCase().toLowerCase()}.")
            }
        }
    }
}