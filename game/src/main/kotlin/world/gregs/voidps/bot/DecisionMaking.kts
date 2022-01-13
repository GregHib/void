package world.gregs.voidps.bot

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.engine.utility.inject

val players: Players by inject()
val tasks: TaskManager by inject()

val scope = CoroutineScope(Contexts.Game)
val logger = InlineLogger("Bot")

on<Registered> { bot: Bot ->
    if (bot.contains("task") && !bot.contains("task_started")) {
        val name: String = bot["task"]
        val task = tasks.get(name)
        if (task == null) {
            bot.clear("task")
        } else {
            assign(bot, task)
        }
    }
}

on<World, AiTick> {
    players.forEach { player ->
        if (player.isBot) {
            val bot: Bot = player["bot"]
            if (!bot.contains("task")) {
                assign(bot, tasks.assign(bot))
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

fun assign(bot: Bot, task: Task) {
    logger.debug { "Task assigned: ${bot.player.accountName} - ${task.name}" }
    bot["task"] = task.name
    bot["task_started"] = true
    task.spaces--
    scope.launch {
        try {
            task.block.invoke(bot)
        } catch (t: Throwable) {
            logger.warn(t) { "Task cancelled for ${bot.player}" }
        }
        bot.clear("task")
        task.spaces++
    }
}