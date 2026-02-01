package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.action.*
import content.bot.fact.Condition
import content.bot.interact.path.Graph
import content.bot.interact.path.Graph.Companion.loadGraph
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.type.random

/**
 * Each tick checks
 *  1. Assigns [activities] if a bot has none.
 *  2. Moves bots onto their next action if they have completed their current.
 *  3. Queues [resolvers] when a bot has an activity with unresolved requirements.
 */
class BotManager(
    private val activities: MutableMap<String, BotActivity> = mutableMapOf(),
    private val resolvers: MutableMap<String, MutableList<Resolver>> = mutableMapOf(),
    private val groups: MutableMap<String, MutableList<String>> = mutableMapOf(),
) : Runnable {
    lateinit var graph: Graph
    val slots = ActivitySlots()
    val bots = mutableListOf<Bot>()
    private val logger = InlineLogger("BotManager")

    fun add(bot: Bot) {
        bots.add(bot)
        for (activity in activities.values) {
            if (activity.requires.any { !it.check(bot.player) }) {
                continue
            }
            bot.available.add(activity.id)
        }
    }

    fun update(bot: Bot, group: String) {
        val iterator = bot.available.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next()
            val activity = activities[id] ?: continue
            // TODO could filter by keys
            if (activity.requires.any { !it.check(bot.player) }) {
                iterator.remove()
            }
        }
        for (id in groups[group] ?: return) {
            val activity = activities[id] ?: continue
            if (activity.requires.any { !it.check(bot.player) }) {
                continue
            }
            bot.available.add(activity.id)
        }
    }

    fun remove(bot: Bot): Boolean {
        if (bots.remove(bot)) {
            stop(bot)
            return true
        }
        return false
    }

    fun load(files: ConfigFiles): BotManager {
        val shortcuts = mutableListOf<NavigationShortcut>()
        loadActivities(files.list(Settings["bots.definitions"]), activities, groups, resolvers, shortcuts)
        graph = loadGraph(files.list(Settings["bots.nav.definitions"]), shortcuts)
        return this
    }

    override fun run() {
        for (bot in bots) {
            tick(bot)
        }
    }

    fun tick(bot: Bot) {
        if (bot.noTask()) {
            assignRandom(bot)
            return
        }
        execute(bot)
    }

    private fun hasRequirements(bot: Bot, activity: BotActivity): Boolean {
        return slots.hasFree(activity) && !bot.blocked.contains(activity.id) && activity.requires.all { it.check(bot.player) }
    }

    fun assign(bot: Bot, id: String): Boolean {
        val activity = activities[id] ?: return false
        assign(bot, activity)
        return true
    }

    private fun assignRandom(bot: Bot) {
        val activity = if (bot.previous != null && hasRequirements(bot, bot.previous!!)) {
            bot.previous
        } else {
            val id = bot.available.filter {
                val activity = activities[it]
                activity != null && hasRequirements(bot, activity)
            }.randomOrNull(random)
            if (id == null) {
                BotActivity("idle", 2048, actions = listOf(BotAction.Wait(50))) // 30s
            } else {
                activities[id]
            }
        }
        if (activity == null) {
            if (bot.player["debug", false]) {
                logger.info { "No activities with requirements met for bot: ${bot.player.accountName}." }
            }
            return
        }
        assign(bot, activity)
    }

    private fun assign(bot: Bot, activity: BotActivity) {
        AuditLog.event(bot, "assigned", activity.id)
        if (bot.player["debug", false]) {
            logger.info { "Assigned bot: '${bot.player.accountName}' task '${activity.id}'." }
        }
        slots.occupy(activity)
        bot.previous = activity
        bot.queue(BehaviourFrame(activity))
    }

    private fun start(bot: Bot, behaviour: Behaviour, frame: BehaviourFrame) {
        for (requirement in behaviour.requires) {
            if (requirement.check(bot.player)) {
                continue
            }
            frame.fail(Reason.Requirement(requirement))
            return
        }
        for (requirement in behaviour.resolve) {
            if (requirement.check(bot.player)) {
                continue
            }
            val resolver = pickResolver(bot, requirement, frame)
            if (resolver == null) {
                frame.fail(Reason.Requirement(requirement)) // No way to resolve
                return
            }
            // Attempt resolution
            AuditLog.event(bot, "start_resolver", resolver.id, behaviour.id)
            if (bot.player["debug", false]) {
                logger.info { "Starting resolution: ${resolver.id} for ${behaviour.id} req ${requirement}." }
            }
            frame.blocked.add(resolver.id)
            val resolverFrame = BehaviourFrame(resolver)
            bot.queue(resolverFrame)
            resolverFrame.start(bot)
            return
        }
        AuditLog.event(bot, "start_activity", behaviour.id)
        if (bot.player["debug", false]) {
            logger.info { "Starting activity: ${behaviour.id}." }
        }
        frame.start(bot)
    }

    private fun pickResolver(bot: Bot, condition: Condition, frame: BehaviourFrame): Behaviour? {
        if (condition is Condition.Area) {
            return Resolver("go_to_${condition.area}", -1, actions = listOf(BotAction.GoTo(condition.area)), produces = setOf(condition))
        }
        // TODO actions should have retry policies?
        val options = mutableListOf<Resolver>()
        for (key in condition.keys()) {
            for (resolver in resolvers[key] ?: return null) {
                if (frame.blocked.contains(resolver.id)) {
                    continue
                }
                if (resolver.requires.any { fact -> !fact.check(bot.player) }) {
                    continue
                }
                options.add(resolver)
            }
        }
        return options.minByOrNull { it.weight }
    }

    private fun execute(bot: Bot) {
        val frame = bot.frame()
        val behaviour = frame.behaviour
        when (val state = frame.state) {
            BehaviourState.Running -> frame.update(bot)
            BehaviourState.Pending -> start(bot, behaviour, frame)
            BehaviourState.Success -> if (!frame.next()) {
                AuditLog.event(bot, "completed", frame.behaviour.id)
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
                        AuditLog.event(bot, "retry", frame.behaviour.id, frame.index, frame.retries, action::class.simpleName)
                        return
                    }
                }

                if (bot.player["debug", false]) {
                    logger.info { "Failed action: ${action::class.simpleName} for ${behaviour.id}, reason: ${state.reason}." }
                }
                AuditLog.event(bot, "failed", behaviour.id, state.reason, frame.index, frame.retries, action::class.simpleName)
                if (state.reason is HardReason) {
                    stop(bot)
                } else {
                    bot.frames.pop()
                }
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