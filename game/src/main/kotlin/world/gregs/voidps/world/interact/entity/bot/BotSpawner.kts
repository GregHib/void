package world.gregs.voidps.world.interact.entity.bot

import kotlinx.coroutines.*
import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject
import java.util.*

val scheduler: Scheduler by inject()
val bots = mutableListOf<Player>()

val loginQueue: LoginQueue by inject()
val factory: PlayerFactory by inject()
val players: Players by inject()

on<World, Startup> {
//    spawnBots(1)
}

/*on<Command>({ prefix == "bot" }) { player: Player ->
    spawnBots(1)
}*/

on<Command>({ prefix == "goto" }) { player: Player ->
    val bot = players.indexed.first { it != null && it.name.startsWith("Bot") } ?: return@on
    println(bot.goTo(when (content) {
        "bank" -> lumbridgeCastleBank
        "axes" -> bobsAxeShop
        else -> return@on
    }))
}

var counter = 0
val varrock = Tile(3212, 3428)
val lumbridge = Rectangle(3221, 3217, 3222, 3220)

val bobsAxeShop = Rectangle(3227, 3201, 3233, 3205)
val lumbridgeCastleBank = Rectangle(3207, 3215, 3210, 3222, 2)

fun spawnBots(count: Int) {
    repeat(count) {
        GlobalScope.launch(Contexts.Game) {
            val name = "Bot ${++counter}"
            val index = loginQueue.login(name)!!
            val bot = Player(index = index, tile = lumbridge.random(), name = name)
            factory.initPlayer(bot, index)
            loginQueue.await()
            bot.initBot()
            bot.login()
            scheduler.launch {
                delay(1)
                bot.viewport.loaded = true
                delay(2)
                bot.action.type = ActionType.None
                bots.add(bot)
                bot.movement.running = true
            }
        }
    }

}