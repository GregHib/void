package world.gregs.voidps.world.activity.skill.mining

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Ore
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.dnd.shootingstar.StarDustHandler

val objects: GameObjects by inject()
val itemDefinitions: ItemDefinitions by inject()

objectOperate("Mine") {
    if (target.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@objectOperate
    }
    val isStar = target.id.startsWith("crashed_star")
    if(isStar){
        val isEarlyBird = StarDustHandler.invokeIsEarlyBird(player)
        if(isEarlyBird!!){
            player.message("Congratulations!, You were the first person to find this star!")
            val xpToAdd:Double = player.levels.get(Skill.Mining) * 75.0
            player.experience.add(Skill.Mining, xpToAdd)
        }
    }
    player.softTimers.start("mining")
    var first = true
    while (true) {
        if (!objects.contains(target)) {
            break
        }

        if (player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more ore.")
            break
        }

        val rock: Rock? = target.def.getOrNull("mining")
        if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
            break
        }

        val pickaxe = getBestPickaxe(player)
        if (!hasRequirements(player, pickaxe, true) || pickaxe == null) {
            break
        }

        val delay = if (pickaxe.id == "dragon_pickaxe" && random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
        if (first) {
            player.message("You swing your pickaxe at the rock.", ChatType.Filter)
            first = false
        }
        val remaining = player.remaining("skill_delay")
        if (remaining < 0) {
            player.face(target)
            player.setAnimation("${pickaxe.id}_swing_low")
            player.start("skill_delay", delay)
            pause(delay)
        } else if (remaining > 0) {
            pause(delay)
        }
        if (rock.gems) {
            val glory = player.equipped(EquipSlot.Amulet).id.startsWith("amulet_of_glory_")
            if (success(player.levels.get(Skill.Mining), if (glory) 3..3 else 1..1)) {
                addOre(player, gems.random())
                continue
            }
        }
        var ores = rock.ores
        if (target.id == "rune_essence_rocks") {
            val name = if (World.members && player.has(Skill.Mining, 30)) "pure_essence" else "rune_essence"
            ores = rock.ores.filter { it == name }
        }
        for (item in ores) {
            val ore = itemDefinitions.get(item)["mining", Ore.EMPTY]
            if (success(player.levels.get(Skill.Mining), ore.chance)) {
                player.experience.add(Skill.Mining, ore.xp)

                if (!addOre(player, item) || deplete(rock, target)) {
                    player.clearAnimation()
                    break
                }
            }
        }
        player.stop("skill_delay")
    }
    player.softTimers.stop("mining")
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
    return pickaxes.firstOrNull { pickaxe -> hasRequirements(player, pickaxe, false) && player.holdsItem(pickaxe.id) }
}

fun hasRequirements(player: Player, pickaxe: Item?, message: Boolean = false): Boolean {
    if (pickaxe == null) {
        if (message) {
            player.message("You need a pickaxe to mine this rock.")
            player.message("You do not have a pickaxe which you have the mining level to use.")
        }
        return false
    }
    return player.hasRequirementsToUse(pickaxe, message, setOf(Skill.Mining, Skill.Firemaking))
}

fun addOre(player: Player, ore: String): Boolean {
    if (ore == "stardust") {
        StarDustHandler.invokeCollectedStarDust()
        val totalStarDust = player.inventory.count(ore) + player.bank.count(ore)
        if (totalStarDust >= 200) {
            player.message("You have the maximum amount of stardust but was still rewarded experience.")
            return true
        }
    }
    val added = player.inventory.add(ore)
    if (added) { player.message("You manage to mine some ${ore.toLowerSpaceCase()}.")
    } else {
        player.inventoryFull()
    }
    println(added)
    return added
}

fun deplete(rock: Rock, obj: GameObject): Boolean {
    if (obj.id.startsWith("crashed_star_tier_")) {
        StarDustHandler.invokeHandleMinedStarDust(obj)
        return false
    }
    if (rock.life >= 0) {
            objects.replace(obj, "depleted${obj.id.dropWhile { it != '_' }}", ticks = rock.life)
        return true
        }
    return false
}

objectApproach("Prospect") {
    if (target.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@objectApproach
    }
    if (player.queue.contains("prospect")) {
        return@objectApproach
    }
    player.message("You examine the rock for ores...")
    player.start("movement_delay", 4)
    player.softQueue("prospect", 4) {
        val ore = def.getOrNull<Rock>("mining")?.ores?.firstOrNull()
        if (ore == null) {
            player.message("This rock contains no ore.")
        } else {
            player.message("This rock contains ${ore.toLowerSpaceCase()}.")
        }
    }
}