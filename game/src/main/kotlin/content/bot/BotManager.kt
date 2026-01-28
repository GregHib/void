package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.action.*
import content.bot.fact.Fact
import content.bot.fact.MandatoryFact
import content.bot.fact.ResolvableFact
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.random

class BotManager(
    private var activities: Map<String, BotActivity> = emptyMap(),
    private var resolvers: Map<Fact, List<Resolver>> = emptyMap(),
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
        return slots.hasFree(activity) && !bot.blocked.contains(activity.id) && activity.requires.all { it is MandatoryFact && it.satisfied(bot) }
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
        for (requirement in behaviour.requires) {
            if (requirement.satisfied(bot)) {
                continue
            }
            if (requirement is MandatoryFact) {
                frame.fail(Reason.Requirements)
                return
            } else if (requirement is ResolvableFact) {
                val resolver = pickResolver(bot, requirement, frame)
                if (resolver == null) {
                    frame.fail(Reason.Requirements) // No way to resolve
                    return
                }
                // Attempt resolution
                frame.blocked.add(resolver.id)
                bot.queue(BehaviourFrame(resolver))
                return
            }
        }
        frame.start(bot)
    }

    // TODO read resolvers
    //  Handle resolver execution and fallback
    private fun pickResolver(bot: Bot, fact: ResolvableFact, frame: BehaviourFrame): Behaviour? {
        val options = mutableListOf<Resolver>()
        for (resolver in resolvers[fact] ?: return null) {
            if (frame.blocked.contains(resolver.id)) {
                continue
            }
            if (resolver.requires.any { it is MandatoryFact && !it.satisfied(bot) }) {
                continue
            }
            options.add(resolver)
        }
        return options.randomOrNull(random)
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