package world.gregs.voidps.world.interact.entity.item

import world.gregs.voidps.ai.inverse
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.InteractFloorItem
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val itemDefs: ItemDefinitions by inject()

val hasInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isNotFull().toDouble() }
val isNotBusy: BotContext.(Any) -> Double = { bot.movement.frozen.toDouble().inverse() }
val itemDesire: BotContext.(FloorItem) -> Double = { item -> bot.desiredItems[item.name] ?: 0.0 }

val pickupItem = SimpleBotOption(
    name = "pickup item",
    targets = { bot.viewport.items.toList() },
    weight = 0.6,
    considerations = listOf(
        isNotBusy,
        hasInventorySpace,
        itemDesire
    ),
    action = { item ->
        bot.instructions.tryEmit(InteractFloorItem(id = item.id, x = item.tile.x, y = item.tile.y, option = 2))
    }
)

val dropDesire: BotContext.(IndexedValue<String>) -> Double = { (_, item) -> bot.desiredItems[item]?.inverse() ?: 0.0 }

val noInventoryOverlayOpen: BotContext.(Any) -> Double = {
    (bot.interfaces.get("overlay_tab") == null).toDouble()
}

val dropItem = SimpleBotOption(
    name = "drop item",
    targets = {  bot.inventory.getItems().withIndex().toList() },
    weight = 0.2,
    considerations = listOf(
        dropDesire,
        noInventoryOverlayOpen
    ),
    action = { (index, item) ->
        bot.instructions.tryEmit(InteractInterface(149, 0, itemDefs.getId(item), index, 7))
    }
)

on<Registered>({ it.isBot }) { bot: Player ->
    bot.botOptions.add(dropItem)
    bot.botOptions.add(pickupItem)
}