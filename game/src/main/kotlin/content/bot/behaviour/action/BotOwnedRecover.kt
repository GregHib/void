package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.owned.Loadout
import content.bot.owned.OwnedBank
import content.bot.owned.OwnedLoadouts
import content.bot.owned.OwnedRoster
import content.bot.owned.owner
import content.entity.player.bank.bank
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get

/**
 * At the booth after a flight (the bank screen is open on the bot): puts the preset the owner last loaded onto
 * this bot back on, when there is one, then takes enough food from the owner's bank to eat back to full
 * hitpoints ([BotOwnedHeal] follows). The food is the preset's, else what the bot carried when it fled, else
 * whatever in the bank heals most. Tells the owner what it managed.
 */
class BotOwnedRecover : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Failed(Reason.Invalid("Owner is offline."))
        if (player.menu != "bank") {
            return BehaviourState.Failed(Reason.Invalid("The bank isn't open."))
        }
        val slot = player["owned_slot", 0]
        val done = mutableListOf<String>()
        val missed = mutableListOf<String>()
        val name = OwnedRoster.preset(owner, slot)
        val preset = name?.let { OwnedLoadouts.get(owner, it) }
        if (name != null && preset == null) {
            missed.add("find the preset '$name'")
        }
        if (name != null && preset != null) {
            val failed = OwnedLoadouts.orders(player, preset).filter { BotOwnedErrand.carryOut(owner, player, it) == null }
            if (failed.isEmpty()) {
                done.add("put '$name' back on")
            } else {
                missed.add("fully put '$name' back on (${failed.joinToString { it.describe() }})")
            }
        }
        val missing = player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution)
        if (missing > 0) {
            val food = food(owner, player, preset)
            if (food == null) {
                missed.add("find any food in your bank")
            } else {
                val wanted = ((missing + heals(food) - 1) / heals(food)).coerceAtLeast(1)
                val got = OwnedBank.withdraw(owner, player, food, wanted)
                val label = ItemDefinitions.get(food).name.lowercase()
                if (got > 0) done.add("took $got $label to eat") else missed.add("take any $label")
            }
        }
        player.clear("owned_flee_food")
        player.closeInterfaces()
        val prefix = "[Companion ${slot + 1}] ${player.name}"
        if (done.isNotEmpty()) {
            owner.message("$prefix ${done.joinToString()}.")
        }
        if (missed.isNotEmpty()) {
            owner.message("$prefix couldn't ${missed.joinToString()}.")
        }
        return BehaviourState.Success
    }

    /** The food to heal with: the preset's, else what was carried at the flight, else the bank's best. */
    private fun food(owner: Player, player: Player, preset: Loadout?): String? {
        val bank = owner.bank
        preset?.carried?.keys?.firstOrNull { edible(it) && bank.indexOf(it) != -1 }?.let { return it }
        player.get<String>("owned_flee_food")?.takeIf { bank.indexOf(it) != -1 }?.let { return it }
        var best: String? = null
        var most = 0
        for (index in bank.indices) {
            val item = bank[index]
            if (item.isEmpty() || !edible(item.id)) {
                continue
            }
            val heal = heals(item.id)
            if (heal > most) {
                most = heal
                best = item.id
            }
        }
        return best
    }

    companion object {
        fun edible(id: String): Boolean {
            val definition = get<ItemDefinitions>().getOrNull(id) ?: return false
            return definition.options.contains("Eat") && definition.contains("heals")
        }

        /** The most one of [id] heals; ten when unknown. */
        fun heals(id: String): Int = get<ItemDefinitions>().getOrNull(id)?.getOrNull<IntRange>("heals")?.last?.coerceAtLeast(1) ?: 10
    }
}
