package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.skill.woodcutting.tree.Tree
import world.gregs.voidps.world.interact.entity.bot.*

val objects: Objects by inject()
val trees = Rectangle(3220, 3244, 3234, 3249)

val isNotAtTrees: BotContext.(Any) -> Double = { (bot.tile !in trees).toDouble() }
val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val hasInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isNotFull().toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = { bot.woodcuttingDesire }
val doesNotWantToStoreLogs: BotContext.(Any) -> Double = { bot.logStorageDesire.inverse() }
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
val isPreferredTree: BotContext.(GameObject) -> Double = {
    val desire: String = bot["desiredLog", ""]
    val tree = Tree.get(it)
    if(tree == null || !bot.has(Skill.Woodcutting, tree.level)) {
        0.0
    } else if(desire.isNotBlank()) {
        (desire == tree.log?.id).toDouble()
    } else {
        val level = (bot.levels.get(Skill.Woodcutting) / 10) + 1
        (level - ((tree.level / 10) + 1)).toDouble().scale(0.0, level.toDouble()).inverse().sine()
    }
}
val isNotBusy: BotContext.(Any) -> Double = { if (bot.isInteruptable()) 0.75 else (bot.action.type == ActionType.None).toDouble() }

val cutDownTree = SimpleBotOption(
    name = "cut tree",
    targets = { bot.tile.chunk.area(2).flatMap { objects[it] }.filter { it.def.options[0] == "Chop down" || it.def.options[0] == "Chop" } },
    weight = 0.5,
    considerations = listOf(
        isNotBusy,
        hasInventorySpace,
        wantsToCutTrees,
        hasEquipmentToCutTrees,
        isNearestTree,
        isPreferredTree
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
        { bot.inventory.count.toDouble().scale(0.0, 28.0) },
        doesNotWantToStoreLogs
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