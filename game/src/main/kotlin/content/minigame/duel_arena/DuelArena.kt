package content.minigame.duel_arena

import content.entity.player.bank.bank
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move

class DuelArena : Script {

    init {
        entered("duel_arena_walkways") {
            open("duel_overlay")
            if (duel == null) {
                options.set(CHALLENGE_SLOT, "Challenge")
            }
        }

        exited("duel_arena_walkways") {
            close("duel_overlay")
            options.remove("Challenge")
        }

        objectOperate("View", "duel_arena_scoreboard") {
            DuelScoreboard.open(this)
        }

        // Stakes and winnings left behind by a crash or a full inventory
        playerSpawn {
            if (inventories.contains("_dueloffer")) {
                otherStake.clear()
            }
            if (inventories.contains("dueloffer")) {
                returnItems(stake)
            }
            if (inventories.contains("duelwinnings")) {
                returnItems(winnings)
            }
        }
    }

    companion object {
        const val CHALLENGE_SLOT = 1

        /**
         * Moves everything in [inventory] to the player's inventory, falling back to their bank
         */
        fun Player.returnItems(inventory: Inventory) {
            var banked = false
            for (index in inventory.indices) {
                if (inventory[index].isEmpty()) {
                    continue
                }
                if (inventory.move(index, this.inventory)) {
                    continue
                }
                if (inventory.move(index, bank)) {
                    banked = true
                }
            }
            if (banked) {
                message("Some of your duel items have been sent to your bank.")
            }
        }
    }
}
