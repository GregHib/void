package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.action.*
import content.bot.fact.Condition
import content.bot.fact.Fact
import content.bot.fact.Predicate
import content.bot.fact.Requirement
import content.bot.interact.path.Graph
import content.bot.interact.path.Graph.Companion.loadGraph
import content.entity.player.bank.bank
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

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

    val activityNames: Set<String>
        get() = activities.keys

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
        loadActivities(files, activities, groups, resolvers, shortcuts)
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
        try {
            execute(bot)
        } catch (exception: Exception) {
            logger.error(exception) { "Error in bot '${bot.player.accountName}' tick ${bot.frames.map { it.behaviour.id }}." }
        }
    }

    private fun hasRequirements(bot: Bot, activity: BotActivity): Boolean {
        return slots.hasFree(activity) && !bot.blocked.contains(activity.id) && activity.requires.all { it.check(bot.player) }
    }

    fun assign(bot: Bot, id: String): Boolean {
        val activity = activities[id] ?: return false
        assign(bot, activity)
        return true
    }

    private val idle = BotActivity("idle", 2048, actions = listOf(BotAction.Wait(TimeUnit.SECONDS.toTicks(30))))

    private fun assignRandom(bot: Bot) {
        val activity = if (bot.previous != null && hasRequirements(bot, bot.previous!!)) {
            bot.previous
        } else {
            if (bot.player["debug", false]) {
                logger.trace { "Picking bot ${bot.player.accountName} new task from available: ${bot.available}." }
            }
            val id = bot.available.filter {
                val activity = activities[it]
                activity != null && hasRequirements(bot, activity)
            }.randomOrNull(random) // TODO weight by distance?
            if (id == null) {
                if (bot.player["debug", false]) {
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
            activities[id] ?: idle
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
        for (requirement in behaviour.setup) {
            if (requirement.check(bot.player)) {
                continue
            }
            val resolvers = availableResolvers(bot, requirement)
            val resolver = resolvers
                .filter { !frame.blocked.contains(it.id) && it.requires.none { fact -> !fact.check(bot.player) } }
                .minByOrNull { it.weight }
            if (resolver == null) {
                if (bot.player["debug", false]) {
                    logger.info { "No resolver found for for ${behaviour.id} keys: ${requirement.fact.keys()} requirement: ${requirement}." }
                    for (resolver in resolvers) {
                        if (frame.blocked.contains(resolver.id)) {
                            logger.debug { "Resolver: ${resolver.id} - Blocked by frame behaviour: ${frame.behaviour.id}." }
                            break
                        }
                        for (requirement in resolver.requires) {
                            if (!requirement.check(bot.player)) {
                                logger.debug { "Resolver: ${resolver.id} - Failed requirement: $requirement." }
                                break
                            }
                        }
                    }
                }
                frame.fail(Reason.Requirement(requirement)) // No way to resolve
                return
            }
            // Attempt resolution
            AuditLog.event(bot, "start_resolver", resolver.id, behaviour.id)
            if (bot.player["debug", false]) {
                logger.info { "Starting resolution: ${resolver.id} for ${behaviour.id} requirement: ${requirement}." }
            }
            frame.blocked.add(resolver.id)
            val resolverFrame = BehaviourFrame(resolver)
            bot.queue(resolverFrame)
            return
        }
        AuditLog.event(bot, "start_activity", behaviour.id)
        if (bot.player["debug", false]) {
            logger.info { "Starting activity: ${behaviour.id}." }
        }
        bot.blocked.add(behaviour.id)
        frame.start(bot)
    }

    private fun availableResolvers(bot: Bot, condition: Requirement<*>): MutableList<Resolver> {
        val options = mutableListOf<Resolver>()
        addDefaultResolvers(bot, options, condition)
        for (key in condition.fact.keys()) {
            options.addAll(resolvers[key] ?: continue)
        }
        return options
    }

    private fun addDefaultResolvers(bot: Bot, resolvers: MutableList<Resolver>, requirement: Requirement<*>) {
        val predicate = requirement.predicate
        if (predicate is Predicate.InArea) {
            resolvers.add(Resolver("go_to_${predicate.name}", -1, actions = listOf(BotAction.GoTo(predicate.name)), produces = setOf(requirement)))
        } else if (predicate is Predicate.IntRange && requirement.fact is Fact.InventoryCount && bot.player.bank.contains(requirement.fact.id, predicate.min!!)) {
            if (predicate.min == 1 || predicate.min == 5 || predicate.min == 10) {
                resolvers.add(
                    Resolver(
                        "withdraw_${requirement.fact.id}", weight = 20,
                        setup = listOf(
                            Requirement(Fact.InventorySpace, Predicate.IntRange(predicate.min))
                        ),
                        actions = listOf(
                            BotAction.GoToNearest("bank"),
                            BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.Equals(Fact.InterfaceOpen("bank"), true)),
                            BotAction.InterfaceOption("Withdraw-${predicate.min}", "bank:inventory:${requirement.fact.id}"),
                            BotAction.CloseInterface,
                        )
                    )
                )
            } else {
                resolvers.add(
                    Resolver(
                        "withdraw_${requirement.fact.id}", weight = 20,
                        setup = listOf(
                            Requirement(Fact.InventorySpace, Predicate.IntRange(predicate.min))
                        ),
                        actions = listOf(
                            BotAction.GoToNearest("bank"),
                            BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.Equals(Fact.InterfaceOpen("bank"), true)),
                            BotAction.InterfaceOption("Withdraw-X", "bank:inventory:${requirement.fact.id}"),
                            BotAction.IntEntry(predicate.min),
                            BotAction.CloseInterface,
                        )
                    )
                )
            }
        } else if (predicate is Predicate.IntRange && requirement.fact is Fact.EquipCount) {
            resolvers.add(
                Resolver(
                    "equip_${requirement.fact.id}", weight = 0,
                    setup = listOf(
                        Requirement(Fact.InventoryCount(requirement.fact.id), Predicate.IntRange(predicate.min))
                    ),
                    actions = listOf(BotAction.InterfaceOption("Equip", "inventory:inventory:${requirement.fact.id}"))
                )
            )
            resolvers.add(
                Resolver(
                    "withdraw_and_equip_${requirement.fact.id}", weight = 0,
                    requires = listOf(Requirement(Fact.BankCount(requirement.fact.id), Predicate.IntRange(predicate.min))),
                    setup = listOf(Requirement(Fact.InventorySpace, Predicate.IntRange(predicate.min))),
                    actions = listOf(
                        BotAction.GoToNearest("bank"),
                        BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.Equals(Fact.InterfaceOpen("bank"), true)),
                        BotAction.InterfaceOption("Withdraw-X", "bank:inventory:${requirement.fact.id}"),
                        BotAction.IntEntry(predicate.min!!),
                        BotAction.CloseInterface,
                        BotAction.InterfaceOption("Equip", "inventory:inventory:${requirement.fact.id}")
                    )
                )
            )
        }
    }

    private fun execute(bot: Bot) {
        val frame = bot.frame()
        val behaviour = frame.behaviour
        if (bot.player["debug", false]) {
            logger.trace { "Bot task: ${behaviour.id} state: ${frame.state} action: ${frame.action()}." }
            bot["previous_state"] = frame.state
        }
        when (val state = frame.state) {
            BehaviourState.Running -> frame.update(bot)
            BehaviourState.Pending -> start(bot, behaviour, frame)
            BehaviourState.Success -> {
                val debug = bot.player["debug", false]
                if (debug) {
                    logger.debug { "Completed action: ${frame.action()} for ${behaviour.id}." }
                }
                if (!frame.next()) {
                    AuditLog.event(bot, "completed", frame.behaviour.id)
                    bot.frames.pop()
                    if (!bot.noTask()) {
                        bot.frame().blocked.remove(behaviour.id)
                    }
                    if (behaviour is BotActivity) {
                        bot.blocked.remove(behaviour.id)
                        slots.release(behaviour)
                    }
                } else {
                    if (debug) {
                        logger.debug { "Next action: ${frame.action()} for ${behaviour.id}." }
                    }
                    frame.start(bot)
                }
            }
            is BehaviourState.Failed -> {
                val action = frame.action()
                if (bot.player["debug", false]) {
                    logger.warn { "Failed action: ${action::class.simpleName} for ${behaviour.id}, reason: ${state.reason}." }
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
            is BehaviourState.Wait -> {
                if (--state.ticks <= 0) {
                    frame.state = state.next
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