package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.type.intEntry
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class RunecraftingGuildRewards : Script {
    init {
        interfaceOpened("rc_guild_rewards") {
            tab(Tab.Inventory)
        }

        interfaceOption("Buy 1", "rc_guild_rewards:*") {
            buy(it.component, 1)
        }

        interfaceOption("Buy X", "rc_guild_rewards:*") {
            val amount = intEntry("Enter amount:") // TODO proper message
            buy(it.component, amount)
        }
    }

    private fun Player.buy(item: String, amount: Int) {
    }
}
