package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val areas: Areas by inject()

val isNotAtBank: BotContext.(Any) -> Double = {
    val area: MapArea? = bot.getOrNull("area")// TODO extension to simplify
    (area == null || !area.values.containsKey("bank")).toDouble()
}
val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val hasNoInventorySpace: BotContext.(Any) -> Double = { bot.inventory.count.toDouble().scale(0.0, 28.0).exponential(7.0) }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }
val wantsToStoreLogs: BotContext.(Any) -> Double = { bot.logStorageDesire }
val hasLogsInInventory: BotContext.(Any) -> Double = { bot.inventory.getItems().maxOfOrNull { bot.desiredItemStorage.getOrDefault(it.name, 0.0) } ?: 0.0 }

val goToBank = SimpleBotOption(
    name = "go to bank",
    targets = { areas.getTagged("bank") },
    weight = 0.5,
    considerations = listOf(
        isNotAtBank,
        isNotGoingSomewhere,
        hasNoInventorySpace,
        wantsToCutTrees,
        wantsToStoreLogs,
        hasLogsInInventory
    ),
    action = {
        bot.goTo(it)
    }
)

val isNearestBank: BotContext.(GameObject) -> Double = { bot.tile.distanceTo(it.tile).toDouble().scale(0.0, 20.0).inverse().logit() }
val isNotBusy: BotContext.(Any) -> Double = { if (bot.isInteruptable()) 0.75 else (bot.action.type == ActionType.None).toDouble() }

fun Player.isInteruptable() = action.type == ActionType.Movement

val openBankBooth = SimpleBotOption(
    name = "open bank",
    targets = { bot.viewport.objects.filter { it.def.options[1] == "Use-quickly" } },
    considerations = listOf(
        isNotBusy,
        hasNoInventorySpace,
        wantsToCutTrees,
        wantsToStoreLogs,
        isNearestBank,
        hasLogsInInventory
    ),
    action = { bank ->
        bot.instructions.tryEmit(InteractObject(objectId = bank.id, x = bank.tile.x, y = bank.tile.y, option = 2))
    }
)


val isLogs: BotContext.(IndexedValue<Item>) -> Double = { (_, id) -> (Log.get(id.name) != null).toDouble() }
val bankIsOpen: BotContext.(Any) -> Double = { (bot.action.type == ActionType.Bank).toDouble() }

val depositLogs = SimpleBotOption(
    name = "deposit all logs into bank",
    targets = { bot.inventory.getItems().withIndex() },
    considerations = listOf(
        bankIsOpen,
        isLogs,
        wantsToStoreLogs
    ),
    action = { (slot, item) ->
        bot.instructions.tryEmit(InteractInterface(interfaceId = 763, componentId = 0, itemId = item.id, itemSlot = slot, option = 5))
    }
)

val exitBank = SimpleBotOption(
    name = "exit bank",
    targets = { empty },
    weight = 0.5,
    action = {
        bot.instructions.tryEmit(CloseInterface)
        bot.instructions.tryEmit(InteractInterface(interfaceId = 762, componentId = 43, itemId = -1, itemSlot = -1, option = 0))
    }
)

on<Registered>({ it.isBot }) { bot: Player ->
    bot.botOptions.add(goToBank)
    bot.botOptions.add(openBankBooth)
}

val dynamicOptions = arrayOf(
    depositLogs,
    exitBank
)

on<InterfaceOpened>({ it.isBot && name == "bank" }) { bot: Player ->
    bot.botOptions.addAll(dynamicOptions)
}

on<InterfaceClosed>({ it.isBot && name == "bank" }) { bot: Player ->
    bot.botOptions.removeAll(dynamicOptions)
}
