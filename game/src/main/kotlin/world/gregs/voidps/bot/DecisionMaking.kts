package world.gregs.voidps.bot

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.utility.inject

val players: Players by inject()
val tasks: TaskManager by inject()

val scope = CoroutineScope(Contexts.Game)
val logger = InlineLogger("Bot")

on<World, AiTick> {
    runBlocking {
        coroutineScope {
            players.forEach { player ->
                if (player.isBot) {
                    val bot: Bot = player["bot"]
                    if (!player.contains("task")) {
                        tasks.assign(bot).let { task ->
                            logger.debug { "Task assigned: ${player.name} - ${task.name}" }
                            player["task"] = true
                            task.spaces--
                            scope.launch {
                                task.block.invoke(bot)
                                player.clear("task")
                                task.spaces++
                            }
                        }
                    }
                    launch(Contexts.Updating) {
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