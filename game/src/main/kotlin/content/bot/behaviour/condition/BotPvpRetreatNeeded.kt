package content.bot.behaviour.condition

import content.area.wilderness.inMultiCombat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

/**
 * True when a PvP bot in a single-combat zone has run out of food.
 *
 * Used to gate the dangerous-arena exit-portal action: the bot retreats only when standing in
 * a single-combat tile and no edible item remains in inventory. In multi-combat areas the bot
 * would die before reaching the portal, so the check returns false there.
 */
class BotPvpRetreatNeeded : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        if (player.inMultiCombat) return false
        val inv = player.inventory
        for (i in inv.indices) {
            if (inv[i].def.options.contains("Eat")) return false
        }
        return true
    }
}
