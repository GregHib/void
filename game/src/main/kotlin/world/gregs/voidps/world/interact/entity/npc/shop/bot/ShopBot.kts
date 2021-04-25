import world.gregs.voidps.ai.inverse
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*
import world.gregs.voidps.world.interact.entity.npc.shop.Price
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer

val desireToUseShop: BotContext.(NPC) -> Double = { npc ->
    val buy = desireToBuy(bot, npc)
    val sell = desireToSell(bot, npc)
    if (buy > sell) {
        if (bot.inventory.isFull() || bot.inventory.contains("coins")) {
            0.0
        } else {
            buy
        }
    } else {
        sell
    }
}

val containerDefs: ContainerDefinitions by inject()
val itemDefs: ItemDefinitions by inject()

fun desireToBuy(bot: Player, npc: NPC): Double {
    if (!bot.inventory.contains("coins")) {
        return 0.0
    }
    if (bot.inventory.isFull()) {
        return 0.0
    }
    val shop = npc.def.getOrNull("shop") as? String ?: return 0.0
    val container = containerDefs.get(shop)
    return container.ids?.maxOf { desireToBuy(bot, itemDefs.getName(it)) } ?: 0.0
}

fun desireToSell(bot: Player, npc: NPC): Double {
    val shop = npc.def.getOrNull("shop") as? String ?: return 0.0
    val container = containerDefs.get(shop)
    return container.ids?.maxOf { desireToSell(bot, itemDefs.getName(it)) } ?: 0.0
}

fun desireToSell(bot: Player, item: String): Double {
    if (item.isBlank()) {
        return 0.0
    }
    return bot.desiredItems[item]?.inverse() ?: return 0.0
}

fun desireToBuy(bot: Player, item: String): Double {
    if (item.isBlank()) {
        return 0.0
    }
    return bot.desiredItems.getOrDefault(item, 0.0)
}

val shops = mutableListOf<NPC>()

on<Registered>({ it.def.has("shop") }) { npc: NPC ->
    shops.add(npc)
}

val notGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }

val notAtShop: BotContext.(NPC) -> Double = { npc -> bot.viewport.npcs.current.contains(npc).toDouble().inverse() }

val goToShop = SimpleBotOption(
    name = "go to shop",
    targets = { shops },
    weight = 0.75,
    considerations = listOf(
        notGoingSomewhere,
        notAtShop,
        // TODO distance
        desireToUseShop
    ),
    action = { npc ->
        bot.goTo(npc.tile)// TODO need to get the nearest/linked node to a shop. Link npc or shop def with node, or node with npc or shop, or even a separate tagging within nav-graph?
    }
)

val openShop = SimpleBotOption(
    name = "open shop",
    targets = { bot.viewport.npcs.current.filter { it.def.options.contains("Trade") } },
    weight = 0.75,
    considerations = listOf(
        desireToUseShop
    ),
    action = { npc ->
        bot.instructions.tryEmit(InteractNPC(npcIndex = npc.index, option = 3))
    }
)

val itemDefinitions: ItemDefinitions by inject()

val desireToSellItem: BotContext.(String) -> Double = { desireToSell(bot, it) }

val relativeItemValue: BotContext.(String) -> Double = { 0.0 }

val desireToBuyItem: BotContext.(IndexedValue<String>) -> Double = { (_, item) -> desireToBuy(bot, item) }

val sellItemToShop = SimpleBotOption(
    name = "sell item",
    targets = { bot.inventory.getItems().toList() },
    weight = 1.0,
    considerations = listOf(
        { (bot.inventory.spaces > 0 || bot.inventory.contains(bot["shop_currency", "coins"])).toDouble() },
        relativeItemValue,
        desireToSellItem
    ),
    action = { item ->
        bot.instructions.tryEmit(InteractInterface(
            interfaceId = 621,
            componentId = 0,
            itemId = itemDefinitions.getId(item),
            itemSlot = bot.inventory.indexOf(item),
            option = 1// Sell 1
        ))
    }
)

val hasSpace: BotContext.(Any) -> Double = { (bot.inventory.spaces > 0).toDouble() }

val inStock: BotContext.(IndexedValue<String>) -> Double = { (index, _) -> (bot.shopContainer(false).getAmount(index) > 0).toDouble() }

val takeItemFromShop = SimpleBotOption(
    name = "take item",
    targets = { bot.shopContainer(true).getItems().withIndex().toList() },
    weight = 0.9,
    considerations = listOf(
        hasSpace,
        desireToBuyItem,
        inStock
    ),
    action = { (index, item) ->
        bot.instructions.tryEmit(InteractInterface(
            interfaceId = 620,
            componentId = 26,
            itemId = -1,
            itemSlot = bot.shopContainer(true).indexOf(item) * 4,
            option = 1// Take 1
        ))
    }
)

val hasCoins: BotContext.(IndexedValue<String>) -> Double = { (index, item) -> (bot.inventory.contains("coins") && bot.inventory.getCount("coins") >= Price.getPrice(bot, itemDefs.getId(item), index, 1)).toDouble() }

val buyItemFromShop = SimpleBotOption(
    name = "buy item",
    targets = { bot.shopContainer(false).getItems().withIndex().toList() },
    weight = 0.8,
    considerations = listOf(
        hasSpace,
        desireToBuyItem,
        inStock,
        hasCoins
    ),
    action = { (index, item) ->
        bot.instructions.tryEmit(InteractInterface(
            interfaceId = 620,
            componentId = 25,
            itemId = -1,
            itemSlot = index * 6,
            option = 1// Buy 1
        ))
        bot.desiredItems.remove(item)
    }
)

val exitShop = SimpleBotOption(
    name = "exit shop",
    targets = { listOf(this) },
    weight = 0.5,
    action = {
        bot.instructions.tryEmit(CloseInterface)
        bot.instructions.tryEmit(InteractInterface(
            interfaceId = 620,
            componentId = 18,
            itemId = -1,
            itemSlot = -1,
            option = 0
        ))
    }
)

on<Registered>({ it.isBot }) { bot: Player ->
    bot.botOptions.add(goToShop)
    bot.botOptions.add(openShop)
}

val dynamicOptions = arrayOf(
    sellItemToShop,
    takeItemFromShop,
    buyItemFromShop,
    exitShop
)

on<InterfaceOpened>({ it.isBot && name == "shop" }) { bot: Player ->
    bot.botOptions.addAll(dynamicOptions)
    bot.botOptions.remove(openShop)
}

on<InterfaceClosed>({ it.isBot && name == "shop" }) { bot: Player ->
    bot.botOptions.removeAll(dynamicOptions)
    bot.botOptions.add(openShop)
}