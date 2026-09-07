package content.minigame.duel_arena

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.BankDeposit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
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
            save(this)
        }
    }

    companion object {
        const val CHALLENGE_SLOT = 1
        private val logger = InlineLogger()

        /**
         * Moves everything in [inventory] to the player's inventory, falling back to their bank
         */
        fun Player.returnItems(inventory: Inventory) {
            var banked = false
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.isEmpty()) {
                    continue
                }
                if (inventory.move(index, this.inventory)) {
                    continue
                }
                BankDeposit.deposit(this, inventory, item, item.amount, index, check = false)
                if (inventory[index].isEmpty()) {
                    banked = true
                } else {
                    logger.warn { "Unable to return duel item $item to $this" }
                }
            }
            if (banked) {
                message("Some of your duel items have been sent to your bank.")
            }
        }

        /**
         * Persist both sides straight after items change hands so a crash can't restore a stale stake
         */
        fun save(vararg players: Player) {
            val queue: SaveQueue = get()
            for (player in players) {
                queue.save(player)
            }
        }
    }
}
