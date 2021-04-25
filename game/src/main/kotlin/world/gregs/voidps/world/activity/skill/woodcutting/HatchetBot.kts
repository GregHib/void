package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.ai.scale
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Boosted
import world.gregs.voidps.engine.entity.character.player.skill.Leveled
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.instruct.InteractFloorItem
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val itemDefs: ItemDefinitions by inject()
val floorItems: FloorItems by inject()
val containerDefs: ContainerDefinitions by inject()
val bobsAxeShop = Rectangle(3227, 3201, 3233, 3205)

val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }

val isNotAtShop: BotContext.(Any) -> Double = { (bot.tile !in bobsAxeShop).toDouble() }
val hasInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isNotFull().toDouble() }

val shopHasBetterHatchetThanCurrent: BotContext.(Any) -> Double = {
    val current = (Hatchet.get(bot)?.index ?: -1) + 1
    val best = (bestShopHatchet(bot)?.index ?: -1) + 1
    (best - current).toDouble().scale(0.0, current.toDouble())
}

fun bestShopHatchet(bot: Player) = Hatchet.regular.lastOrNull { hatchet -> Hatchet.hasRequirements(bot, hatchet) && shopContains(hatchet) }

fun shopContains(hatchet: Hatchet): Boolean {
    val ids = containerDefs.get("bobs_brilliant_axes").ids ?: return false
    return ids.contains(itemDefs.getId(hatchet.id))
}

val floorHatchetIsBetterThanCurrent: BotContext.(FloorItem) -> Double = { floorItem ->
    isBetterHatchet(bot, floorItem).toDouble()
}

fun isBetterHatchet(bot: Player, item: FloorItem): Boolean {
    val current = Hatchet.get(bot) ?: return true
    val target = Hatchet.get(item.def.name) ?: return false
    return current.ordinal < target.ordinal
}

val howMuchBetterThanCurrent: BotContext.(FloorItem) -> Double = { floorItem ->
    val current = (Hatchet.get(bot)?.ordinal ?: -1) + 1
    val best = (Hatchet.highest(bot)?.ordinal ?: -1) + 1
    val option = (Hatchet.get(floorItem.def.name)?.ordinal ?: -1) + 1
    (option - current).toDouble().scale(0.0, best.toDouble())
}

val meetsLevelRequirement: BotContext.(FloorItem) -> Double = { floorItem ->
    Hatchet.hasRequirements(bot, Hatchet.get(floorItem.def.name)).toDouble()
}

val goToHatchetShop = SimpleBotOption(
    name = "go to bobs axe shop",
    targets = { listOf(this) },
    weight = 0.75,
    considerations = listOf(
        isNotGoingSomewhere,
        isNotAtShop,
        wantsToCutTrees,
        hasInventorySpace,
        shopHasBetterHatchetThanCurrent
    ),
    action = {
        bot.goTo(bobsAxeShop)
    }
)

val pickupHatchet = SimpleBotOption(
    name = "pickup hatchet",
    targets = { bot.tile.chunk.area(2).flatMap { floorItems[it] }.filter { Hatchet.isHatchet(it.def.name) }.toList() },
    considerations = listOf(
        hasInventorySpace,
        meetsLevelRequirement,
        floorHatchetIsBetterThanCurrent,
        howMuchBetterThanCurrent
    ),
    action = { hatchet ->
        bot.instructions.tryEmit(InteractFloorItem(id = hatchet.id, x = hatchet.tile.x, y = hatchet.tile.y, option = 2))
    }
)

val inventoryHatchets: BotContext.() -> List<Triple<Hatchet, Int, String>> = {
    bot.inventory.getItems().withIndex().mapNotNull {
        val hatchet = Hatchet.get(itemDefs.get(it.value).name)
        if (hatchet == null) null else Triple(hatchet, it.index, it.value)
    }
}

val isWorseThanCurrent: BotContext.(Triple<Hatchet, Int, String>) -> Double = { (hatchet) ->
    val current = Hatchet.get(bot)
    (current != null && hatchet.ordinal < current.ordinal).toDouble()
}

val dropOldHatchet = SimpleBotOption(
    name = "drop old hatchet",
    targets = inventoryHatchets,
    considerations = listOf(isWorseThanCurrent),
    action = { (_, slot, item) ->
        bot.instructions.tryEmit(InteractInterface(149, 0, itemDefs.getId(item), slot, 7))
    }
)

val betterThanEquippedHatchet: BotContext.(Triple<Hatchet, Int, String>) -> Double = { (hatchet) ->
    val currentWeapon = bot.equipment.getItem(EquipSlot.Weapon.index)
    if (currentWeapon.isBlank()) {
        1.0
    } else {
        val current = Hatchet.get(currentWeapon)
        (current != null && current.ordinal < hatchet.ordinal).toDouble()
    }
}

val equipHatchet = SimpleBotOption(
    name = "equip hatchet if better than or no weapon",
    targets = inventoryHatchets,
    considerations = listOf(betterThanEquippedHatchet),
    action = { (_, slot, item) ->
        bot.instructions.tryEmit(InteractInterface(149, 0, itemDefs.getId(item), slot, 1))
    }
)

on<Registered>({ it.isBot }) { bot: Player ->
    bot.botOptions.add(dropOldHatchet)
    bot.botOptions.add(pickupHatchet)
    bot.botOptions.add(equipHatchet)
    updateHatchetDesire(bot)
}

on<Boosted>({ it.isBot }) { bot: Player ->
    updateHatchetDesire(bot)
}

on<Leveled>({ it.isBot }) { bot: Player ->
    updateHatchetDesire(bot)
}

on<ItemChanged>({ Hatchet.isHatchet(item) }) { bot: Player ->
    updateHatchetDesire(bot)
}

fun updateHatchetDesire(bot: Player) {
    val current = (Hatchet.get(bot)?.ordinal ?: -1) + 1
    val best = (Hatchet.highest(bot)?.ordinal ?: -1) + 1
    Hatchet.regular.forEach { hatchet ->
        if (Hatchet.hasRequirements(bot, hatchet, false)) {
            // Hatchet desire = how much better it is than the current hatchet
            val option = hatchet.ordinal + 1
            bot.desiredItems[hatchet.id] = (option - current).toDouble().scale(0.0, best.toDouble())
        } else {
            bot.desiredItems.remove(hatchet.id)
        }
    }
}