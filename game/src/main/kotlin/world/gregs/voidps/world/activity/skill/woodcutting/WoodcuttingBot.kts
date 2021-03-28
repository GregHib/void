package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.ai.exponential
import world.gregs.voidps.ai.inverse
import world.gregs.voidps.ai.scale
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.*

val objects: Objects by inject()
val trees = Rectangle(3220, 3244, 3234, 3249)

val isNotAtTrees: BotContext.(Any) -> Double = { (bot.tile !in trees).toDouble() }
val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val hasInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isNotFull().toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }
val hasEquipmentToCutTrees: BotContext.(Any) -> Double = { (Hatchet.get(bot) != null).toDouble() }

val goToTrees = SimpleBotOption(
    name = "go to trees",
    targets = { listOf(this) },
    weight = 0.5,
    considerations = listOf(
        isNotAtTrees,
        isNotGoingSomewhere,
        hasInventorySpace,
        wantsToCutTrees,
        hasEquipmentToCutTrees
    ),
    action = {
        bot.goTo(trees)
    }
)

val isNearestTree: BotContext.(GameObject) -> Double = { bot.tile.distanceTo(it.tile).toDouble().scale(0.0, 20.0).inverse().exponential() }
val isNotBusy: BotContext.(Any) -> Double = { if (bot.isInteruptable()) 0.75 else (bot.action.type == ActionType.None).toDouble() }

val cutDownTree = SimpleBotOption(
    name = "cut tree",
    targets = { bot.tile.chunk.area(2).flatMap { objects[it] }.filter { it.def.name == "Tree" && it.def.options[0] == "Chop down" } },
    weight = 0.5,
    considerations = listOf(
        isNotBusy,
        hasInventorySpace,
        wantsToCutTrees,
        isNearestTree,
        hasEquipmentToCutTrees
    ),
    action = { tree ->
        if ((bot.isInteruptable() && last?.target != tree) || bot.action.type == ActionType.None) {
            bot.instructions.tryEmit(InteractObject(tree.id, tree.tile.x, tree.tile.y, 1))
        }
    }
)

fun Player.isInteruptable() = action.type == ActionType.Woodcutting || action.type == ActionType.Movement

val dropLogs = SimpleBotOption(
    name = "drop logs",
    targets = { listOf(this) },
    considerations = listOf(
        { bot.impatience },
        { bot.inventory.count.toDouble().scale(0.0, 28.0) }
    ),
    action = {
        var count = 0
        for ((slot, item) in bot.inventory.getItems().withIndex()) {
            if (item == 1511) {
                bot.instructions.tryEmit(InteractInterface(149, 0, item, slot, 7))
                if (++count == 2) {
                    break
                }
            }
        }
    }
)

val wait = SimpleBotOption(
    name = "wait around",
    targets = { listOf(this) },
    weight = 0.2,
    considerations = listOf(
        { bot.patience }
    ),
    action = {}
)

on<Player, Registered>({ it.isBot }) {
    it.botOptions.add(cutDownTree)
    it.botOptions.add(goToTrees)
    it.botOptions.add(dropLogs)
    it.botOptions.add(wait)
}