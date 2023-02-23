package world.gregs.voidps.bot

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import java.util.concurrent.ConcurrentLinkedQueue

val players: Players by inject()
val tasks: TaskManager by inject()

val scope = CoroutineScope(Contexts.Game)
val logger = InlineLogger("Bot")

onBot<Registered> { bot: Bot ->
    if (bot.hasVar("task") && !bot.hasVar("task_started")) {
        val name: String = bot["task"]
        val task = tasks.get(name)
        if (task == null) {
            bot.clearVar("task")
        } else {
            assign(bot, task)
        }
    }
}

on<World, AiTick> {
    players.forEach { player ->
        if (player.isBot) {
            val bot: Bot = player["bot"]
            if (!bot.hasVar("task")) {
                assign(bot, tasks.assign(bot))
            }
            val events: ConcurrentLinkedQueue<Event> = player["events"]
            while (events.isNotEmpty()) {
                val event = events.poll()
                bot.botEvents.emit(event)
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
        bot.clearVar("task")
        task.spaces++
    }
}