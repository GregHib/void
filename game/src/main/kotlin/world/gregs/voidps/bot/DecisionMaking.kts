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
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject

val players: Players by inject()
val tasks: TaskManager by inject()

val scope = CoroutineScope(Contexts.Game)
val logger = InlineLogger("Bot")

on<Registered> { bot: Bot ->
    if (bot.contains("task")) {
        val name: String = bot["task"]
        val tasks: TaskManager = get()
        val task = tasks.get(name)
        if (task == null) {
            bot.clear("task")
        } else {
            assign(bot, task)
        }
    }
}

on<World, AiTick> {
    runBlocking {
        coroutineScope {
            players.forEach { player ->
                if (player.isBot) {
                    val bot: Bot = player["bot"]
                    if (!player.contains("task")) {
                        assign(bot, tasks.assign(bot))
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

fun assign(bot: Bot, task: Task) {
    logger.debug { "Task assigned: ${bot.player.name} - ${task.name}" }
    bot["task"] = task.name
    task.spaces--
    scope.launch {
        task.block.invoke(bot)
        bot.clear("task")
        task.spaces++
    }
}