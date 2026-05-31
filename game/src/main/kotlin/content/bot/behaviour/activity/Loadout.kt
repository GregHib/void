package content.bot.behaviour.activity

import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup

/**
 * Named gear set used by hybrid PvP bots.
 * - [equipment] is the worn kit when this loadout is active.
 * - [extraInventory] is additional carry items the loadout depends on (e.g. runes for a magic
 *   loadout). Pre-stocked into the bot's inventory at spawn alongside the items from other loadouts.
 */
data class Loadout(
    val name: String,
    val equipment: BotEquipmentSetup,
    val extraInventory: BotInventorySetup?,
    val autocast: String? = null,
)
