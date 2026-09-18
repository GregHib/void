package content.minigame.duel_arena

import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class DuelStake : Script {

    val stakeRestriction = object : ItemRestrictionRule {
        override fun restricted(id: String): Boolean {
            val def = ItemDefinitions.get(id)
            return def.lendTemplateId != -1 || def.dummyItem != 0 || !def["tradeable", true]
        }
    }

    init {
        playerSpawn {
            stake.itemRule = stakeRestriction
        }

        interfaceOption(id = "duel2_side:offer") { (item, _, option) ->
            val amount = when (option) {
                "Stake-1" -> 1
                "Stake-5" -> 5
                "Stake-10" -> 10
                "Stake-All" -> Int.MAX_VALUE
                "Stake-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            offer(this, item.id, amount)
        }

        interfaceOption(id = "stake:offer") { (item, itemSlot, option) ->
            val amount = when (option) {
                "Remove-1" -> 1
                "Remove-5" -> 5
                "Remove-10" -> 10
                "Remove-All" -> stake.count(item.id)
                "Remove-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            remove(this, item.id, itemSlot, amount)
        }

        // Mirror the stake to the opponent's secondary container and reset any accepts
        slotChanged("dueloffer") { change ->
            val duel = duel ?: return@slotChanged
            val opponent = duel.opponent(this)
            opponent.otherStake.transaction { set(change.index, change.item, change.from, change.fromIndex) }
            DuelRulesScreen.resetAccept(duel)
        }
    }

    fun staking(player: Player, amount: Int): Boolean {
        val duel = player.duel ?: return false
        return duel.staked && duel.stage == DuelStage.Rules && amount >= 1
    }

    fun offer(player: Player, id: String, amount: Int) {
        if (!staking(player, amount)) {
            return
        }
        val offered = player.inventory.transaction {
            val added = removeToLimit(id, amount)
            val transaction = link(player.stake)
            transaction.add(id, added)
        }
        if (!offered) {
            player.message("That item cannot be staked.")
        }
    }

    fun remove(player: Player, id: String, slot: Int, amount: Int) {
        if (!staking(player, amount)) {
            return
        }
        player.stake.transaction {
            val added = link(player.inventory).addToLimit(id, amount)
            if (!inventory.stackable(id) && added == 1) {
                clear(slot)
            } else {
                removeToLimit(id, added)
            }
        }
    }
}
