package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.action.*
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.random

class BotManager(
    private var activities: Map<String, BotActivity> = emptyMap(),
) : Runnable {
    val slots = ActivitySlots()
    val bots = mutableListOf<Bot>()
    val logger = InlineLogger("BotManager")

    fun load(files: ConfigFiles): BotManager {
        activities = loadActivities(files.list(Settings["bots.definitions"]))
        return this
    }

    override fun run() {
        for (bot in bots) {
            tick(bot)
        }
    }

    fun tick(bot: Bot) {
        if (bot.noTask()) {
            assignActivity(bot)
            return
        }
        execute(bot)
    }

    private fun hasRequirements(bot: Bot, activity: BotActivity): Boolean {
        return slots.hasFree(activity) && !bot.blocked.contains(activity.id) && activity.requirements.all { it.satisfied(bot) }
    }

    private fun assignActivity(bot: Bot) {
        val activity = if (bot.previous != null && hasRequirements(bot, bot.previous!!)) {
            bot.previous!!
        } else {
            activities.values
                .filter { hasRequirements(bot, it) }
                .randomOrNull(random) ?: return
        }
        logger.info { "Assigned bot: ${bot.player.accountName} task ${activity.id}." }
        slots.occupy(activity)
        bot.previous = activity
        bot.queue(BehaviourFrame(activity))
    }

    private fun start(bot: Bot, behaviour: Behaviour, frame: BehaviourFrame) {
        if (behaviour.requirements.any { !it.satisfied(bot) }) {
            frame.fail(Reason.Requirements)
            return
        }
        frame.start(bot)
    }

    private fun execute(bot: Bot) {
        val frame = bot.frame()
        val behaviour = frame.behaviour
        when (val state = frame.state) {
            BehaviourState.Running -> return
            BehaviourState.Pending -> start(bot, behaviour, frame)
            BehaviourState.Success -> if (!frame.next()) {
                bot.frames.pop()
                if (behaviour is BotActivity) {
                    slots.release(behaviour)
                }
            }
            is BehaviourState.Failed -> {
                val action = frame.action()
                if (action is BotAction.RetryableAction && action.retryMax > 0) {
                    frame.state = BehaviourState.Wait(action.retryTicks)
                    if (frame.retries++ < action.retryMax) {
                        return
                    }
                }
                bot.frames.pop()
                if (behaviour is BotActivity) {
                    bot.blocked.add(behaviour.id)
                    slots.release(behaviour)
                }
            }
            is BehaviourState.Wait -> {
                if (--state.ticks <= 0) {
                    frame.state = BehaviourState.Pending
                }
            }
        }
    }

    private fun stop(bot: Bot) {
        for (frame in bot.frames) {
            if (frame.behaviour is BotActivity) {
                slots.release(frame.behaviour)
            }
        }
        bot.reset()
    }

}