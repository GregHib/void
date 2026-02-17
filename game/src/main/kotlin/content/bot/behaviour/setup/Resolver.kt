package content.bot.behaviour.setup

import content.bot.behaviour.Behaviour
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * A behaviour that can be performed to resolve a requirement of another [Resolver] or [BotAction]
 * E.g. buying a pickaxe from a shop, getting items out of the bank, picking up an item off of the floor
 * [weight] specifies resolver preference; lower is more likely to be chosen.
 */
data class Resolver(
    override val id: String,
    val weight: Int,
    override val timeout: Int = TimeUnit.MINUTES.toTicks(1),
    override val requires: List<Condition> = emptyList(),
    override val setup: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<String> = emptySet(),
) : Behaviour
