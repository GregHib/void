package world.gregs.voidps.world.activity.skill.mining

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.definition.data.MiningRock
import world.gregs.voidps.engine.entity.definition.data.Ore
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.requiredEquipLevel
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTitleCase
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

                val rock: MiningRock? = obj.def.getOrNull("mining")
                if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
                    break
                }

                val pickaxe = getBestPickaxe(player)
                if (!hasRequirements(player, pickaxe, true) || pickaxe == null) {
                    break
                }

                val delay = if (pickaxe.id == "dragon_pickaxe" && Random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
                if (first) {
                    player.message("You swing your pickaxe at the rock.", ChatType.GameFilter)
                    player.start("skilling_delay", delay)
                    first = false
                }
                player.face(obj)
                player.setAnimation("${pickaxe.id}_swing_low")
                delay(delay)
                if (rock.gems) {
                    if (success(player.levels.get(Skill.Mining), 1..1)) {
                        addOre(player, gems.random())
                    }
                }
                for (item in rock.ores) {
                    val ore = item.def["mining", Ore.EMPTY]
                    if (success(player.levels.get(Skill.Mining), ore.chance)) {
                        player.experience.add(Skill.Mining, ore.xp)

                        if (!addOre(player, item.id) || deplete(rock, obj)) {
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

val gems = setOf(
    "uncut_sapphire",
    "uncut_emerald",
    "uncut_ruby",
    "uncut_diamond"
)

val pickaxes = listOf(
    Item("dragon_pickaxe"),
    Item("volatile_clay_pickaxe"),
    Item("sacred_clay_pickaxe"),
    Item("inferno_adze"),
    Item("rune_pickaxe"),
    Item("adamant_pickaxe"),
    Item("mithril_pickaxe"),
    Item("steel_pickaxe"),
    Item("iron_pickaxe"),
    Item("bronze_pickaxe")
)

fun getBestPickaxe(player: Player): Item? {
    return pickaxes.firstOrNull { pickaxe -> hasRequirements(player, pickaxe, false) && player.hasItem(pickaxe.id) }
}

fun hasRequirements(player: Player, pickaxe: Item?, message: Boolean = false): Boolean {
    if (pickaxe == null) {
        if (message) {
            player.message("You need a pickaxe to mine this rock.")
            player.message("You do not have a pickaxe which you have the mining level to use.")
        }
        return false
    }
    if (pickaxe.id == "inferno_adze" && !player.has(Skill.Firemaking, pickaxe.def["fm_level", 1], message)) {
        return false
    }
    if (!player.has(Skill.Mining, pickaxe.def["mining_level", pickaxe.def.requiredEquipLevel()], message)) {
        return false
    }
    return true
}

fun addOre(player: Player, ore: String): Boolean {
    val added = player.inventory.add(ore)
    if (added) {
        player.message("You manage to mine some ${ore.toTitleCase().lowercase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}

fun deplete(rock: MiningRock, obj: GameObject): Boolean {
    if (rock.life >= 0) {
        obj.replace("depleted${obj.id.dropWhile { it != '_' }}", ticks = rock.life)
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
            val ore = obj.def.getOrNull<MiningRock>("mining")?.ores?.firstOrNull()
            if (ore == null) {
                player.message("This rock contains no ore.")
            } else {
                player.message("This rock contains ${ore.id.toTitleCase().lowercase()}.")
            }
        }
    }
}