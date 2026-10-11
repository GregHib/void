package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.item
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Games room prize chest which holds the owners prize money.
 * One game at a time can be offered the prize, the winner is given a prize key to take it from the chest with.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HousePrizeChest : Script {
    private val cooldownMinutes = 3 // Guessed

    init {
        val chests = "oak_prize_chest,teak_prize_chest,mahogany_prize_chest"
        objectOperate("Open", chests) { (target) ->
            chest(target)
        }

        itemOnObjectOperate("prize_key", chests) { (target) ->
            chest(target)
        }
    }

    /**
     * Tells the player how much is in the [chest] and what it's being played for, and lets them take it with a prize key or add coins.
     */
    private suspend fun Player.chest(chest: GameObject) {
        val owner = houseOwner() ?: return
        val prize = owner["house_prize_money", 0]
        val id = owner.get<String>("house_prize_game")
        val status = when (id) {
            null -> "No game is currently being played for the prize."
            FAIRY -> "The prize will be awarded to the finder of the treasure fairy."
            else -> "The prize will be awarded to the winner of the ${if (id == "hoop") "hoop and stick" else id} game."
        }
        val text = "You must have a prize key or be in building mode to take the prize. Prize amount: <col=7f0000>${prize.toDigitGroupString()}</col>. $status"
        message(text, ChatType.Broadcast)
        item("prize_key", text)
        val own = inOwnHouse()
        val canTake = prize > 0 && (carriesItem("prize_key") || (own && get("house_build_mode", false)))
        val capacity = Tables.int("house_prize_chests.${chest.id}.capacity")
        if (canTake) {
            choice("Select an option") {
                option("Take prize") {
                    takePrize(owner, chest)
                }
                if (own) {
                    option("Add coins") {
                        addCoins(chest, capacity)
                    }
                }
            }
        } else if (own) {
            choice("Add coins to the prize chest?") {
                option("Yes") {
                    addCoins(chest, capacity)
                }
                option("No")
            }
        }
    }

    private fun Player.takePrize(owner: Player, chest: GameObject) {
        val prize = owner["house_prize_money", 0]
        if (prize <= 0) {
            return
        }
        if (!inventory.add("coins", prize)) {
            inventoryFull()
            return
        }
        owner["house_prize_money"] = 0
        inventory.remove("prize_key")
        message("You take the prize.", ChatType.Broadcast)
        openChest(chest)
    }

    private suspend fun Player.addCoins(chest: GameObject, capacity: Int) {
        val last = get("house_prize_added", 0)
        if (last != 0 && GameLoop.tick - last < TimeUnit.MINUTES.toTicks(cooldownMinutes)) {
            message("After you add coins to a chest, you must wait a few minutes before doing so again.", ChatType.Broadcast)
            return
        }
        val stored = get("house_prize_money", 0)

        val amount = intEntry("How much would you like to deposit?").coerceAtMost(capacity - stored) // TODO proper message
        if (amount <= 0) {
            message("The prize chest can't hold any more coins.") // TODO proper message
            return
        }
        if (!inventory.remove("coins", amount)) {
            message("You don't have enough coins.") // TODO proper message
            return
        }
        set("house_prize_money", stored + amount)
        set("house_prize_added", GameLoop.tick)
        message("You add ${amount.toDigitGroupString()} coins.", ChatType.Broadcast)
        openChest(chest)
        sound("coins_jingle_1")
    }

    private fun Player.openChest(chest: GameObject) {
        anim("human_openheavychest")
        areaSound("chest_open", chest.tile, radius = 5)
        chest.replace("${chest.id}_open", ticks = 3)
    }

    companion object {
        const val FAIRY = "fairy"

        /**
         * Lets the owner choose to put the prize up for the game [id] they have just started, if nothing else is being played for it.
         */
        suspend fun Player.offerPrize(id: String) {
            val owner = houseOwner() ?: return
            if (owner != this || owner["house_prize_money", 0] <= 0 || owner.contains("house_prize_game")) {
                return
            }
            choice("Offer the prize for this game?") {
                option("Yes") {
                    owner["house_prize_game"] = id
                }
                option("No")
            }
        }

        /**
         * Gives the winner of the game [id] a prize key if it's the game the prize was offered for.
         */
        fun Player.winPrize(id: String) {
            val owner = houseOwner() ?: return
            if (owner.get<String>("house_prize_game") != id) {
                return
            }
            if (!inventory.add("prize_key")) {
                inventoryFull()
                return
            }
            owner.clear("house_prize_game")
            message("A little key appears in your pack!")
            sound("ping")
        }
    }
}
