package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.ai.exponential
import world.gregs.voidps.ai.inverse
import world.gregs.voidps.ai.scale
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.contains
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val objects: Objects by inject()
val lumbridgeCastleBank = Rectangle(3207, 3215, 3210, 3222, 2)

val isNotAtBank: BotContext.(Any) -> Double = { (bot.tile !in lumbridgeCastleBank).toDouble() }
val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val hasNoInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isFull().toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }
val wantsToStoreLogs: BotContext.(Any) -> Double = { bot.logStorageDesire }
val hasLogsInInventory: BotContext.(Any) -> Double = { bot.inventory.contains("logs").toDouble() }

val goToBank = SimpleBotOption(
    name = "go to bank",
    targets = { listOf(this) },
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
        bot.goTo(lumbridgeCastleBank)
    }
)

val isNearestBank: BotContext.(GameObject) -> Double = { bot.tile.distanceTo(it.tile).toDouble().scale(0.0, 20.0).inverse().exponential() }
val isNotBusy: BotContext.(Any) -> Double = { if (bot.isInteruptable()) 0.75 else (bot.action.type == ActionType.None).toDouble() }

fun Player.isInteruptable() = action.type == ActionType.Movement

val openBankBooth = SimpleBotOption(
    name = "open bank",
    targets = { bot.tile.chunk.area(2).flatMap { objects[it] }.filter { it.def.options[1] == "Use-quickly" } },
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


val isLogs: BotContext.(Pair<Int, Int>) -> Double = { (id, _) -> (definitions.get(id).name == "Logs").toDouble() }
val definitions: ItemDefinitions by inject()
val bankIsOpen: BotContext.(Any) -> Double = { (bot.action.type == ActionType.Bank).toDouble() }

val depositLogs = SimpleBotOption(
    name = "deposit all logs into bank",
    targets = { bot.inventory.getItems().mapIndexed { index, id -> id to index } },
    considerations = listOf(
        bankIsOpen,
        isLogs,
        wantsToStoreLogs
    ),
    action = { (id, slot) ->
        bot.instructions.tryEmit(InteractInterface(interfaceId = 763, componentId = 0, itemId = id, itemSlot = slot, option = 5))
    }
)

val exitBank = SimpleBotOption(
    name = "exit bank",
    targets = { listOf(this) },
    considerations = listOf(
        bankIsOpen
    ),
    weight = 0.5,
    action = {
        bot.instructions.tryEmit(CloseInterface)
        bot.instructions.tryEmit(InteractInterface(interfaceId = 762, componentId = 43, itemId = -1, itemSlot = -1, option = 0))
    }
)

on<Player, Registered>({ it.isBot }) {
    it.botOptions.add(goToBank)
    it.botOptions.add(openBankBooth)
    it.botOptions.add(depositLogs)
    it.botOptions.add(exitBank)
}