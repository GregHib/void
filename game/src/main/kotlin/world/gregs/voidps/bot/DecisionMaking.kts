package world.gregs.voidps.bot

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.entity.AiTick
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject

val players: Players by inject()
val tasks: TaskManager by inject()

val scope = CoroutineScope(Contexts.Game)
val logger = InlineLogger("Bot")

onEvent<Player, StartBot> { bot ->
    if (!bot.contains("task") || bot.contains("task_started")) {
        return@onEvent
    }
    val name: String = bot["task"]!!
    val task = tasks.get(name)
    if (task == null) {
        bot.clear("task")
    } else {
        assign(bot, task)
    }
}

onEvent<World, AiTick> {
    players.forEach { player ->
        if (player.isBot) {
            val bot: Bot = player["bot"]!!
            if (!bot.contains("task")) {
                assign(player, tasks.assign(bot))
            }
            player.bot.resume("tick")
        }
    }
}

fun assign(bot: Player, task: Task) {
    if (bot["debug", false]) {
        logger.debug { "Task assigned: ${bot.accountName} - ${task.name}" }
    }
    bot["task"] = task.name
    bot["task_started"] = true
    task.spaces--
    scope.launch {
        try {
            task.block.invoke(bot)
        } catch (t: Throwable) {
            logger.warn(t) { "Task cancelled for $bot" }
        }
        bot.clear("task")
        task.spaces++
    }
}