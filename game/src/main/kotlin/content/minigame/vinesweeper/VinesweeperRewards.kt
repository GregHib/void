package content.minigame.vinesweeper

import content.entity.player.dialogue.type.intEntry
import content.minigame.vinesweeper.Vinesweeper.Companion.addPoints
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Mrs Winkin's World of Seeds: spend Vinesweeper points on seeds or Farming experience.
 * The client draws the shop itself from the reward enums and the points varbit.
 */
class VinesweeperRewards(val enums: EnumDefinitions) : Script {

    init {
        interfaceOpened("vinesweeper_rewards") {
            interfaceOptions.unlockAll("vinesweeper_rewards", "items", 0 until REWARD_SLOTS * CHILDREN_PER_REWARD)
            interfaces.sendVisibility("vinesweeper_rewards", "confirm", false)
        }

        interfaceOption("Close", "vinesweeper_rewards:close") {
            close("vinesweeper_rewards")
        }

        interfaceOption(id = "vinesweeper_rewards:items") {
            val index = it.itemSlot / CHILDREN_PER_REWARD
            val id = enums.intOrNull("vinesweeper_rewards", index) ?: return@interfaceOption
            if (id == XP_ITEM) {
                if (it.option == "Value") {
                    interfaces.sendVisibility("vinesweeper_rewards", "confirm", true)
                }
                return@interfaceOption
            }
            val item = ItemDefinitions.get(id).stringId
            val price = enums.intOrNull("vinesweeper_reward_prices", id) ?: return@interfaceOption
            when (it.option) {
                "Value" -> message("${ItemDefinitions.get(item).name}: $price points.")
                "Buy 1" -> buy(item, price, 1)
                "Buy 5" -> buy(item, price, 5)
                "Buy 10" -> buy(item, price, 10)
                "Buy X" -> buy(item, price, intEntry("Enter amount:"))
                "Examine" -> message(ItemDefinitions.get(item)["examine", ""])
            }
        }

        interfaceOption("Proceed", "vinesweeper_rewards:proceed") {
            interfaces.sendVisibility("vinesweeper_rewards", "confirm", false)
            tradeForXp()
        }

        interfaceOption("Cancel", "vinesweeper_rewards:cancel") {
            interfaces.sendVisibility("vinesweeper_rewards", "confirm", false)
        }
    }

    private fun Player.buy(item: String, price: Int, amount: Int) {
        if (amount <= 0) {
            return
        }
        if (item == "flag" && inventory.count("flag") + amount > Vinesweeper.MAX_FLAGS) {
            message("You can only carry ${Vinesweeper.MAX_FLAGS} flags at once.")
            return
        }
        val cost = price.toLong() * amount
        if (cost > get("vinesweeper_points", 0)) {
            message("You don't have enough points to buy that.")
            return
        }
        if (!inventory.add(item, amount)) {
            message("You don't have enough inventory space.")
            return
        }
        addPoints(-cost.toInt())
    }

    /**
     * Trade every point for Farming experience; below level 40 only a percentage of each point
     * becomes experience, as defined by the client's rate enum.
     */
    private fun Player.tradeForXp() {
        val points = get("vinesweeper_points", 0)
        if (points <= 0) {
            message("You don't have any points to trade.")
            return
        }
        val xp = experience(points, levels.get(Skill.Farming))
        exp(Skill.Farming, xp.toDouble())
        set("vinesweeper_points", 0)
        message("You trade your $points points for $xp Farming experience.")
    }

    private fun experience(points: Int, level: Int): Int = if (level >= FULL_RATE_LEVEL) {
        points
    } else {
        (points.toLong() * enums.int("vinesweeper_xp_rates", level) / 100).toInt()
    }

    companion object {
        private const val REWARD_SLOTS = 47
        private const val CHILDREN_PER_REWARD = 5
        private const val XP_ITEM = 11209
        private const val FULL_RATE_LEVEL = 40
    }
}
