import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.interact.entity.bot.BotContext
import world.gregs.voidps.world.interact.entity.bot.SimpleBotOption
import world.gregs.voidps.world.interact.entity.bot.botOptions
import world.gregs.voidps.world.interact.entity.bot.isBot
import world.gregs.voidps.world.interact.entity.player.energy.Energy.Companion.energyPercent

val goingSomewhere: BotContext.(Any) -> Double = { (bot["navigating", false]).toDouble() }
val walkToggled: BotContext.(Any) -> Double = { (!bot.running).toDouble() }
val notResting: BotContext.(Any) -> Double = {
    val movement = bot.getVar("movement", "walk")
    (movement == "walk" || movement == "run").toDouble()
}
val resting: BotContext.(Any) -> Double = {
    val movement = bot.getVar("movement", "walk")
    (movement == "rest" || movement == "music").toDouble()
}
val distanceToMusician: BotContext.(NPC) -> Double = { npc ->
    bot.tile.distanceTo(npc.tile).toDouble().scale(0.0, 30.0).inverse().logistic(2.0, 0.23)
}

fun Player.energyScale() = this["energy", 10000].toDouble().scale(0.0, 10000.0)

val lowEnergy: BotContext.(Any) -> Double = {
    bot.energyScale().exponential(3.2).inverse()
}
val anyEnergy: BotContext.(Any) -> Double = {
    (bot.energyPercent() >= 1).toDouble()
}
val fullEnergy: BotContext.(Any) -> Double = {
    bot.energyScale().logistic(3.0, 0.29)
}

val listenToMusician = SimpleBotOption(
    "listen to musician",
    targets = { bot.viewport.npcs.current.filter { it.def.options.contains("Listen-to") } },
    weight = 0.5,
    considerations = listOf(
        goingSomewhere,
        lowEnergy,
        distanceToMusician
    ),
    action = {
        if (bot.action.type != ActionType.Resting) {
            bot.instructions.tryEmit(InteractNPC(npcIndex = it.index, option = 3))// Listen to
        }
    }
)

val stopResting = SimpleBotOption(
    "stop resting",
    targets = { empty },
    weight = 1.0,
    considerations = listOf(
        resting,
        fullEnergy
    ),
    action = {
        val movement = bot.getVar("movement", "walk")
        if (movement == "rest" || movement == "music") {
            bot.instructions.tryEmit(Walk(bot.tile.x, bot.tile.y))
        }
    }
)

val notBusy: BotContext.(Any) -> Double = {
    (bot.action.type == ActionType.None).toDouble()
}

val rest = SimpleBotOption(
    "rest",
    targets = { empty },
    weight = 0.8,
    considerations = listOf(
        notBusy,
        notResting,
        goingSomewhere,
        lowEnergy
    ),
    action = {
        bot.instructions.tryEmit(InteractInterface(interfaceId = 750, componentId = 1, itemId = -1, itemSlot = -1, option = 1))
    }
)

val run = SimpleBotOption(
    "run",
    targets = { empty },
    weight = 1.0,
    considerations = listOf(
        walkToggled,
        anyEnergy
    ),
    action = {
        bot.instructions.tryEmit(InteractInterface(interfaceId = 750, componentId = 1, itemId = -1, itemSlot = -1, option = 0))
    }
)

on<ActionStarted>({ it.isBot && type == ActionType.Resting }) { bot: Player ->
    bot.botOptions.add(stopResting)
}

on<ActionFinished>({ it.isBot && type == ActionType.Resting }) { bot: Player ->
    bot.botOptions.remove(stopResting)
}

on<Registered>({ it.isBot }) { player: Player ->
    player.botOptions.add(listenToMusician)
    player.botOptions.add(rest)
    player.botOptions.add(run)
}