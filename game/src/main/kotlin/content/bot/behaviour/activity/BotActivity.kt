package content.bot.behaviour.activity

import content.bot.behaviour.Behaviour
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * An activity that bots can perform, limited by the capacity of the area it takes place in
 * E.g. cutting oak trees in varrock, mining copper ore in lumbridge
 */
data class BotActivity(
    override val id: String,
    override val timeout: Int = TimeUnit.MINUTES.toTicks(1),
    override val requires: List<Condition> = emptyList(),
    override val setup: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val reactive: List<BotAction> = emptyList(),
    override val produces: Set<String> = emptySet(),
    val loadouts: Map<String, Loadout> = emptyMap(),
    val boundArea: String? = null,
) : Behaviour {

    /**
     * The area condition which decides where this activity takes place and which area's slots it uses.
     */
    val area: BotInArea?
        get() = setup.firstOrNull { it is BotInArea && it.present } as? BotInArea

    /**
     * Copy of this activity limited to the single area [id].
     */
    fun bind(id: String): BotActivity {
        val current = area ?: return copy(boundArea = id)
        return copy(setup = setup.map { if (it === current) BotInArea(id) else it }, boundArea = id)
    }
}
