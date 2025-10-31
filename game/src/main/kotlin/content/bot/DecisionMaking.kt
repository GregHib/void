package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.interact.navigation.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.AiTick
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject
import kotlin.coroutines.resume

class DecisionMaking : Script {

    val players: Players by inject()
    val tasks: TaskManager by inject()

    val scope = CoroutineScope(Contexts.Game)
    val logger = InlineLogger("Bot")

    init {
        onEvent<Player, StartBot> { bot ->
            if (!bot.contains("task_bot") || bot.contains("task_started")) {
                return@onEvent
            }
            val name: String = bot["task_bot"]!!
            val task = tasks.get(name)
            if (task == null) {
                bot.clear("task_bot")
            } else {
                assign(bot, task)
            }
        }

        onEvent<World, AiTick> {
            players.forEach { player ->
                if (player.isBot) {
                    val bot: Bot = player["bot"]!!
                    if (!bot.contains("task_bot")) {
                        val lastTask: String? = bot["last_task_bot"]
                        assign(player, tasks.assign(bot, lastTask))
                    }
                    player.bot.resume("tick")
                    handleNewSuspensions(player)
                }
            }
        }
    }

    fun handleNewSuspensions(player: Player) {
        val suspensions: MutableList<Pair<Player.() -> Boolean, CancellableContinuation<Unit>>> = player["bot_new_suspensions"] ?: return
        suspensions.removeIf {
            val success = it.first(player)
            if (success) it.second.resume(Unit)
            success
        }
    }

    fun assign(bot: Player, task: Task) {
        if (bot["debug", false]) {
            logger.debug { "Task assigned: ${bot.accountName} - ${task.name}" }
        }
        val last = bot.get<String>("task_bot")
        if (last != null) {
            bot["last_task_bot"] = last
        }
        bot["task_bot"] = task.name
        bot["task_started"] = true
        task.spaces--
        scope.launch {
            try {
                task.block.invoke(bot)
            } catch (t: Throwable) {
                logger.warn(t) { "Task cancelled for $bot" }
            }
            bot.clear("task_bot")
            task.spaces++
        }
    }
}
