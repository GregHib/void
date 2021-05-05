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
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.skill.woodcutting.log.Log
import world.gregs.voidps.world.activity.skill.woodcutting.tree.RegularTree
import world.gregs.voidps.world.interact.entity.bot.*
import kotlin.math.max

val areas: Areas by inject()

val isNotAtTrees: BotContext.(MapArea) -> Double = { (bot.tile !in it.area).toDouble() }
val isNotGoingSomewhere: BotContext.(Any) -> Double = { (!bot["navigating", false]).toDouble() }
val hasInventorySpace: BotContext.(Any) -> Double = { bot.inventory.isNotFull().toDouble() }
val wantsToCutTrees: BotContext.(Any) -> Double = {
    max(bot.desiredExperience.getOrDefault(Skill.Woodcutting, 0.0),
        bot.desiredItems.getOrDefault("logs", 0.0))
}
val doesNotWantToStoreLogs: BotContext.(Any) -> Double = { bot.logStorageDesire.inverse() }
val hasEquipmentToCutTrees: BotContext.(Any) -> Double = { (Hatchet.get(bot) != null).toDouble() }

val isPreferredArea: BotContext.(MapArea) -> Double = {
    val trees = it.values["trees"] as List<String>
    val tree: RegularTree? = RegularTree.values().lastOrNull { tree -> trees.contains(tree.id) && bot.has(Skill.Woodcutting, tree.level) }
    treeDesire(bot, tree)
}

val goToTrees = SimpleBotOption(
    name = "go to trees",
    targets = { areas.getTagged("trees") },
    weight = 0.5,
    considerations = listOf(
        isNotAtTrees,
        isNotGoingSomewhere,
        hasInventorySpace,
        wantsToCutTrees,
        hasEquipmentToCutTrees,
        isPreferredArea
    ),
    action = {
        bot.goTo(it)
    }
)

val isNearestTree: BotContext.(GameObject) -> Double = { bot.tile.distanceTo(it.tile).toDouble().scale(0.0, 20.0).inverse().exponential() }
val isPreferredTree: BotContext.(GameObject) -> Double = {
    val tree = RegularTree.get(it)
    treeDesire(bot, tree)
}

fun treeDesire(bot: Player, tree: RegularTree?): Double {
    val desire: String = bot["desiredLog", ""]
    return if (tree == null || !bot.has(Skill.Woodcutting, tree.level)) {
        0.0
    } else if (desire.isNotBlank()) {
        (desire == tree.log?.id).toDouble()
    } else {
        val bestTree = RegularTree.values().last { bot.has(Skill.Woodcutting, it.level) }
        ((bestTree.ordinal + 1) - (tree.ordinal + 1)).toDouble().scale(0.0, bestTree.ordinal + 1.0).inverse()
    }
}

val isNotBusy: BotContext.(Any) -> Double = { if (bot.isInteruptable()) 0.75 else (bot.action.type == ActionType.None).toDouble() }

val cutDownTree = SimpleBotOption(
    name = "cut tree",
    targets = { bot.viewport.objects.filter { it.def.options[0] == "Chop down" || it.def.options[0] == "Chop" } },
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
    targets = { empty },
    considerations = listOf(
        { bot.impatience },
        { bot.inventory.count.toDouble().scale(0.0, 28.0) },
        doesNotWantToStoreLogs
    ),
    action = {
        var count = 0
        for ((slot, item) in bot.inventory.getItems().withIndex()) {
            if (Log.get(item.name) != null) {
                bot.instructions.tryEmit(InteractInterface(149, 0, item.id, slot, 7))
                if (++count == 2) {
                    break
                }
            }
        }
    }
)

val wait = SimpleBotOption(
    name = "wait around",
    targets = { empty },
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