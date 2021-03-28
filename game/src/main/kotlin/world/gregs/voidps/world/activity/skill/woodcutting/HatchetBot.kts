package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.ai.scale
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.instruct.InteractFloorItem
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val definition: ItemDefinitions by inject()
val floorItems: FloorItems by inject()
val bobsAxeShop = Rectangle(3227, 3201, 3233, 3205)

val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }

val isNotAtShop: BotContext.(Any) -> Double = { (bot.tile !in bobsAxeShop).toDouble() }

val shopHasBetterHatchetThanCurrent: BotContext.(Any) -> Double = {
    val bestShopHasToOffer = Hatchet.regular.lastOrNull { hatchet -> Hatchet.hasRequirements(bot, hatchet) && shopContains(hatchet) }
    val current = (Hatchet.get(bot)?.index ?: -1) + 1
    val best = (bestShopHasToOffer?.index ?: -1) + 1
    (best - current).toDouble().scale(0.0, current.toDouble())
}

fun shopContains(hatchet: Hatchet) = hatchet.index < 7

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

val isWorseThanCurrent: BotContext.(Triple<Hatchet, Int, Int>) -> Double = { (hatchet) ->
    val current = Hatchet.get(bot)
    (current != null && hatchet.ordinal < current.ordinal).toDouble()
}

val dropOldHatchet = SimpleBotOption(
    name = "drop old hatchet",
    targets = {
        bot.inventory.getItems().withIndex().mapNotNull {
            val hatchet = Hatchet.get(definition.get(it.value).name)
            if (hatchet == null) null else Triple(hatchet, it.index, it.value)
        }
    },
    considerations = listOf(isWorseThanCurrent),
    action = { (_, slot, item) ->
        bot.instructions.tryEmit(InteractInterface(149, 0, item, slot, 7))
    }
)

on<Player, Registered>({ it.isBot }) {
    it.botOptions.add(goToHatchetShop)
    it.botOptions.add(dropOldHatchet)
    it.botOptions.add(pickupHatchet)
}