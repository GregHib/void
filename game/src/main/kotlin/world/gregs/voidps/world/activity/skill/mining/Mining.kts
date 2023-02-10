package world.gregs.voidps.world.activity.skill.mining

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Ore
import world.gregs.voidps.engine.entity.definition.data.Rock
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.item.requiredEquipLevel
import world.gregs.voidps.engine.entity.members
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.random.Random

val objects: Objects by inject()

on<ObjectOption>({ option == "Mine" }) { player: Player ->
    if (obj.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@on
    }
    var first = true
    while (player.awaitDialogues()) {
        if (objects[obj.tile, obj.id] == null) {
            break
        }

        if (player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more ore.")
            break
        }

        val rock: Rock? = obj.def.getOrNull("mining")
        if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
            break
        }

        val pickaxe = getBestPickaxe(player)
        if (!hasRequirements(player, pickaxe, true) || pickaxe == null) {
            break
        }

        val delay = if (pickaxe.id == "dragon_pickaxe" && Random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
        if (first) {
            player.message("You swing your pickaxe at the rock.", ChatType.Filter)
            first = false
        }
        player.face(obj)
        player.setAnimation("${pickaxe.id}_swing_low")
        pause(delay)
        if (rock.gems) {
            val glory = player.equipped(EquipSlot.Amulet).id.startsWith("amulet_of_glory_")
            if (success(player.levels.get(Skill.Mining), if (glory) 3..3 else 1..1)) {
                addOre(player, gems.random())
                continue
            }
        }
        var ores = rock.ores
        if (obj.id == "rune_essence_rocks") {
            val name = if (World.members && player.has(Skill.Mining, 30)) "pure_essence" else "rune_essence"
            ores = rock.ores.filter { it.id == name }
        }
        for (item in ores) {
            val ore = item.def["mining", Ore.EMPTY]
            if (success(player.levels.get(Skill.Mining), ore.chance)) {
                player.experience.add(Skill.Mining, ore.xp)

                if (!addOre(player, item.id) || deplete(rock, obj)) {
                    player.clearAnimation()
                    break
                }
            }
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
        player.message("You manage to mine some ${ore.toLowerSpaceCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}

fun deplete(rock: Rock, obj: GameObject): Boolean {
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
    player.message("You examine the rock for ores...")
    player.start("delay", 4)
    player.softQueue(4) {
        val ore = def.getOrNull<Rock>("mining")?.ores?.firstOrNull()
        if (ore == null) {
            player.message("This rock contains no ore.")
        } else {
            player.message("This rock contains ${ore.id.toLowerSpaceCase()}.")
        }
    }
}