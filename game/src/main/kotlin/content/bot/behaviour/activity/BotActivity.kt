package content.bot.behaviour.activity

import content.bot.behaviour.Behaviour
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotLoadout
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.loadout.Loadout
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
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
    val prefer: List<Condition> = emptyList(),
    val boundArea: String? = null,
    val boundLoadout: String? = null,
) : Behaviour {

    /**
     * Likelihood of this activity being picked, increased by [bonus] for each [prefer] condition [player] meets.
     */
    fun weight(player: Player, bonus: Int): Int = 1 + prefer.count { it.check(player) } * bonus

    /**
     * The loadout setup requirement which is replaced with concrete items when bound to a bot.
     */
    val loadout: BotLoadout?
        get() = setup.firstOrNull { it is BotLoadout } as? BotLoadout

    /**
     * Copy of this activity with its [loadout] replaced by the concrete [equipment] and [inventory] chosen for a bot.
     * Equipment slots already set by the activity take precedence and inventory items are merged into its existing
     * inventory requirement, as separate inventory requirements would undo each other when withdrawing from the bank.
     */
    fun bind(loadout: Loadout, equipment: Map<EquipSlot, BotItem>, inventory: List<BotItem>): BotActivity {
        val conditions = setup.filter { it !is BotLoadout }.toMutableList()
        if (equipment.isNotEmpty()) {
            val index = conditions.indexOfFirst { it is BotEquipmentSetup }
            if (index == -1) {
                conditions.add(BotEquipmentSetup(equipment))
            } else {
                conditions[index] = BotEquipmentSetup(equipment + (conditions[index] as BotEquipmentSetup).items)
            }
        }
        if (inventory.isNotEmpty()) {
            val index = conditions.indexOfFirst { it is BotInventorySetup }
            if (index == -1) {
                conditions.add(BotInventorySetup(inventory))
            } else {
                conditions[index] = BotInventorySetup((conditions[index] as BotInventorySetup).items + inventory)
            }
        }
        conditions.sortBy { it.priority }
        return copy(setup = conditions, boundLoadout = loadout.id)
    }

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
