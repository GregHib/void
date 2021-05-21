package world.gregs.voidps.bot

import kotlinx.coroutines.*
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.isBot

val players: Players by inject()
val tasks: TaskStore by inject()

val scope = CoroutineScope(Contexts.Game)

on<World, AiTick> {
    runBlocking {
        coroutineScope {
            players.forEach { player ->
                if (player.isBot) {
                    val bot: Bot = player["bot"]
                    launch(Contexts.Updating) {
                        if (!player.contains("task")) {
                            val task = tasks.obtain()
                            GlobalScope.launch(Contexts.Game) {
                                task?.invoke(scope, bot)
                                player["task"] = false
                            }
                            player["task"] = true
                        }
                        val events: MutableList<Event> = player["events"]
                        val iterator = events.iterator()
                        while (iterator.hasNext()) {
                            val event = iterator.next()
                            bot.botEvents.emit(event)
                            iterator.remove()
                        }
                        bot.resume("tick")
                    }
                }
            }
        }
    }
}