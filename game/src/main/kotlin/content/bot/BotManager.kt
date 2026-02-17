package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.Behaviour
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotGameWorld
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.HardReason
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotWait
import content.bot.behaviour.activity.ActivitySlots
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.loadBehaviours
import content.bot.behaviour.setup.DynamicResolvers
import content.bot.behaviour.setup.Resolver
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Manages [Bot] behaviour execution and activity scheduling
 * - Assigns [activities] to idle bots when possible.
 * - Advances the current [BehaviourFrame].
 * - Resolves unmet [BotActivity.setup] requirements.
 * - Handles activity completion, failure, and slot allocation.
 */
class BotManager(
    private val activities: MutableMap<String, BotActivity> = mutableMapOf(),
    val resolvers: MutableMap<String, MutableList<Resolver>> = mutableMapOf(),
    private val groups: MutableMap<String, MutableList<String>> = mutableMapOf(),
    private val world: BotWorld = BotGameWorld(),
) : Runnable {
    internal val slots = ActivitySlots()
    val bots = mutableListOf<Bot>()
    private val logger = InlineLogger("BotManager")

    val activityNames: Set<String>
        get() = activities.keys

    fun load(files: ConfigFiles): BotManager {
        loadBehaviours(files, activities, groups, resolvers)
        return this
    }

    fun add(bot: Bot) {
        bots.add(bot)
        // Update all available activities
        bot.available.clear()
        for (activity in activities.values) {
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

    override fun run() {
        for (bot in bots) {
            tick(bot)
        }
    }

    fun tick(bot: Bot) {
        try {
            if (bot.noTask()) {
                assignRandom(bot)
                return
            }
            execute(bot)
        } catch (exception: Exception) {
            logger.error(exception) { "Error in bot '${bot.player.accountName}' tick ${bot.frames.map { it.behaviour.id }}." }
        }
    }

    /**
     * Assign activity [id] to [bot]
     * Useful for debugging
     */
    fun assign(bot: Bot, id: String): Boolean {
        val activity = activities[id] ?: return false
        assign(bot, activity)
        return true
    }

    /**
     * Assign a random activity that is available to the [bot].
     */
    private fun assignRandom(bot: Bot) {
        if (bot.evaluate.isNotEmpty()) {
            updateAvailable(bot)
        }
        if (bot.player["debug", false]) {
            logger.trace { "Picking new activity from ${bot.available} for bot ${bot.player.accountName}." }
        }
        var activity = if (hasRequirements(bot, bot.previous)) {
            bot.previous!!
        } else {
            bot.available
                .filter { hasRequirements(bot, activities[it]) }
                .randomOrNull(random)
                ?.let { activities[it] }
        }
        if (activity == null) {
            if (bot.player["debug", false]) {
                logger.debug { "No activities with requirements met for bot: ${bot.player.accountName}." }
                debugActivities(bot)
            }
            activity = idle
        }
        assign(bot, activity)
    }

    /**
     * Remove invalid activities, check for new valid activities based on recent state changes ready to [Bot.evaluate].
     */
    private fun updateAvailable(bot: Bot) {
        // Remove activities which are no longer available
        val iterator = bot.available.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next()
            val activity = activities[id] ?: continue
            if (activity.requires.any { !it.check(bot.player) }) {
                iterator.remove()
            }
        }
        // Add activities which have become available
        for (group in bot.evaluate) {
            for (id in groups[group] ?: return) {
                val activity = activities[id] ?: continue
                if (activity.requires.any { !it.check(bot.player) }) {
                    continue
                }
                bot.available.add(activity.id)
            }
        }
        bot.evaluate.clear()
    }

    private val idle = BotActivity("idle", 2048, timeout = TimeUnit.HOURS.toTicks(1), actions = listOf(BotWait(TimeUnit.SECONDS.toTicks(30))))

    private fun hasRequirements(bot: Bot, activity: BotActivity?): Boolean = activity != null && slots.hasFree(activity) && !bot.blocked.contains(activity.id) && activity.requires.all { it.check(bot.player) }

    private fun assign(bot: Bot, activity: BotActivity) {
        AuditLog.event(bot, "assigned", activity.id)
        if (bot.player["debug", false]) {
            logger.info { "Assigned task '${activity.id}' to bot '${bot.player.accountName}'." }
        }
        slots.occupy(activity)
        bot.previous = activity
        bot.queue(BehaviourFrame(activity))
    }

    /**
     * Check and update [bot]'s current activity and action state
     */
    private fun execute(bot: Bot) {
        val frame = bot.frame()
        when (val state = frame.state) {
            BehaviourState.Running -> frame.update(bot, world)
            BehaviourState.Pending -> start(bot, frame)
            BehaviourState.Success -> nextAction(bot, frame)
            is BehaviourState.Failed -> handleFail(bot, frame, state)
            is BehaviourState.Wait -> {
                val behaviour = frame.behaviour
                if (bot.player["debug", false]) {
                    logger.trace { "Bot wait: ${behaviour.id} state: ${frame.state} action: ${frame.action()}." }
                    bot["previous_state"] = frame.state
                }
                if (--state.ticks <= 0) {
                    frame.state = state.next
                }
            }
        }
    }

    /**
     * Check [bot] has requirements to start the current activity
     * Find [resolvers] if activity has set up [Condition]s
     */
    private fun start(bot: Bot, frame: BehaviourFrame) {
        val behaviour = frame.behaviour
        for (requirement in behaviour.requires) {
            if (requirement.check(bot.player)) {
                continue
            }
            frame.fail(Reason.Requirement(requirement))
            return
        }
        val debug = bot.player["debug", false]
        for (requirement in behaviour.setup) {
            if (requirement.check(bot.player)) {
                continue
            }
            frame.blocked.removeAll(DynamicResolvers.ids())
            val resolvers = buildList {
                DynamicResolvers.resolver(bot.player, requirement)?.also { add(it) }
                requirement.keys()
                    .flatMap { resolvers[it].orEmpty() }
                    .forEach(::add)
            }
            val resolver = resolvers
                .filter { !frame.blocked.contains(it.id) && it.requires.none { fact -> !fact.check(bot.player) } }
                .minByOrNull { it.weight }
            if (resolver == null) {
                if (debug) {
                    debugResolvers(behaviour, requirement, resolvers, frame, bot)
                }
                frame.fail(Reason.Requirement(requirement)) // No way to resolve
                return
            }
            // Attempt resolution
            AuditLog.event(bot, "start_resolver", resolver.id, behaviour.id)
            if (debug) {
                logger.info { "Starting ${resolver.id} for ${behaviour.id} requirement: $requirement." }
            }
            frame.blocked.add(resolver.id)
            val resolverFrame = BehaviourFrame(resolver)
            bot.queue(resolverFrame)
            return
        }
        AuditLog.event(bot, "start_activity", behaviour.id)
        if (debug) {
            logger.info { "Starting activity: ${behaviour.id}." }
        }
        bot.blocked.add(behaviour.id)
        frame.start(bot, world)
    }

    /**
     * Move onto the next action in a behaviour or remove the behaviour from the stack if it is completed
     */
    private fun nextAction(bot: Bot, frame: BehaviourFrame) {
        val debug = bot.player["debug", false]
        if (frame.next()) {
            if (debug) {
                logger.debug { "Next action: ${frame.action()} for ${frame.behaviour.id}." }
            }
            frame.start(bot, world)
            return
        }
        val behaviour = frame.behaviour
        if (debug) {
            logger.debug { "Completed action: ${frame.action()} for ${behaviour.id}." }
        }
        AuditLog.event(bot, "completed", frame.behaviour.id)
        bot.frames.pop()
        if (!bot.noTask()) {
            bot.frame().blocked.remove(behaviour.id)
        }
        if (behaviour is BotActivity) {
            bot.blocked.remove(behaviour.id)
            slots.release(behaviour)
        }
    }

    /**
     * Stop the current activity or remove the current resolver
     */
    private fun handleFail(bot: Bot, frame: BehaviourFrame, state: BehaviourState.Failed) {
        val behaviour = frame.behaviour
        val action = frame.action()
        if (bot.player["debug", false]) {
            logger.warn { "Failed ${behaviour.id} action=${action::class.simpleName}, reason=${state.reason}." }
        }
        AuditLog.event(bot, "failed", behaviour.id, state.reason, frame.index, action::class.simpleName)
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

    /**
     * Remove all behaviours and free up activity slots
     */
    private fun stop(bot: Bot) {
        for (frame in bot.frames) {
            if (frame.behaviour is BotActivity) {
                slots.release(frame.behaviour)
            }
        }
        bot.reset()
    }

    private fun debugResolvers(behaviour: Behaviour, requirement: Condition, resolvers: List<Resolver>, frame: BehaviourFrame, bot: Bot) {
        logger.warn { "No resolver found for keys=${requirement.keys()} id=${behaviour.id}, requirement=$requirement." }
        for (resolver in resolvers) {
            if (frame.blocked.contains(resolver.id)) {
                logger.debug { "Resolver ${resolver.id} - Blocked by frame behaviour: ${frame.behaviour.id}." }
                break
            }
            for (requirement in resolver.requires) {
                if (!requirement.check(bot.player)) {
                    logger.debug { "Resolver ${resolver.id} - Failed requirement: $requirement." }
                    return
                }
            }
            logger.debug { "Resolver ${resolver.id} - Available." }
        }
    }

    private fun debugActivities(bot: Bot) {
        logger.info { "Failed to find activity for bot ${bot.player.accountName}." }
        for (id in bot.available) {
            val activity = activities[id] ?: continue
            if (!slots.hasFree(activity)) {
                logger.trace { "Activity: $id - No available slots." }
            } else if (bot.blocked.contains(activity.id)) {
                logger.trace { "Activity: $id - Blocked." }
            } else {
                for (requirement in activity.requires) {
                    if (!requirement.check(bot.player)) {
                        logger.trace { "Activity: $id - Failed requirement: $requirement" }
                        break
                    }
                }
            }
        }
    }
}
