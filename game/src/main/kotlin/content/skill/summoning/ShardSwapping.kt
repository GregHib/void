package content.skill.summoning

import content.entity.player.bank.noted
import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.sub.Interface
import world.gregs.voidps.type.sub.Option
import kotlin.math.min

class ShardSwapping(
    private val enums: EnumDefinitions,
    private val itemDefinitions: ItemDefinitions,
) {

    @Option("Swap", "bogrog")
    fun swap(player: Player, target: NPC) {
        openTradeInInterface(player, true)
    }

    @Option("Trade Scrolls", "summoning_trade_in")
    fun scrolls(player: Player, target: NPC) {
        openTradeInInterface(player, false)
    }

    @Option("Trade Pouches", "summoning_trade_in")
    fun pouches(player: Player, target: NPC) {
        openTradeInInterface(player, true)
    }

    @Interface("Value", "*_trade_in", "summoning_trade_in")
    fun value(player: Player, item: Item, itemSlot: Int, component: String) {
        val enumIndex = (itemSlot + 3) / 5
        var actualItem = item
        val itemType = component.removeSuffix("_trade_in")

        if (item.id.endsWith("_u")) {
            val actualItemId = enums.get("summoning_${itemType}_ids_1").getInt(enumIndex)
            actualItem = Item(itemDefinitions.get(actualItemId).stringId)
        }

        sendValueMessage(player, actualItem, itemType)
    }

    @Interface("Trade*", "*_trade_in", "summoning_trade_in")
    suspend fun trade(player: Player, item: Item, itemSlot: Int, component: String, option: String) {
        val enumIndex = (itemSlot + 3) / 5
        var actualItem = item
        val itemType = component.removeSuffix("_trade_in")

        if (item.id.endsWith("_u")) {
            val actualItemId = enums.get("summoning_${itemType}_ids_1").getInt(enumIndex)
            actualItem = Item(itemDefinitions.get(actualItemId).stringId)
            sendValueMessage(player, actualItem, itemType)
            return
        }

        val amount = when (option) {
            "Trade" -> 1
            "Trade-5" -> 5
            "Trade-10" -> 10
            "Trade-X" -> player.intEntry("Enter amount:")
            "Trade-All" -> Int.MAX_VALUE
            else -> return
        }
        swapForShards(player, actualItem, amount)
    }

    /**
     * Trades scrolls or pouches to Bogrog for shards.
     *
     * @param player: The player trading with Bogrog
     * @param item: The item the player is attempting to swap
     * @param amount: The number of items the player has attempted to swap
     */
    fun swapForShards(player: Player, item: Item, amount: Int) {
        val shardsPerSwap = item.def["shard_refund_amount", 1]
        val itemsNeededPerSwap = item.def["summoning_refund_amount_inverse", 1]
        val maxToSwap = if (amount == Int.MAX_VALUE) getItemCount(player, item) else min(amount * itemsNeededPerSwap, getItemCount(player, item))
        val actualNumberTraded = maxToSwap - (maxToSwap % itemsNeededPerSwap)
        val totalSwaps = actualNumberTraded / itemsNeededPerSwap
        val unnotedItemCount = player.inventory.count(item.id)

        val unnotedToSwap = min(unnotedItemCount, actualNumberTraded)
        val notedToSwap = if (item != item.noted) actualNumberTraded - unnotedToSwap else 0

        player.inventory.transaction {
            if (unnotedToSwap > 0) remove(item.id, unnotedToSwap)
            if (notedToSwap > 0) remove(item.noted!!.id, notedToSwap)
            add("spirit_shards", totalSwaps * shardsPerSwap)
        }
    }

    /**
     * Gets the total number of a given item a player is holding, including noted items.
     *
     * @param player: The player we are counting the inventory of.
     * @param item: The item we are counting
     */
    fun getItemCount(player: Player, item: Item): Int {
        val notedItem = if (item.noted == item) Item.EMPTY else item.noted ?: Item.EMPTY
        val unnotedHeld = player.inventory.count(item.id)
        val notedHeld = player.inventory.count(notedItem.id)

        return notedHeld + unnotedHeld
    }

    /**
     * Sends a message to the given player with the number of shards they will receive
     * for trading in a scroll or pouch.
     *
     * @param player: The player to send the message to
     * @param item: The item that was clicked in the interface
     * @param itemType: The type of item being clicked. "pouch" or "scroll"
     */
    fun sendValueMessage(player: Player, item: Item, itemType: String) {
        val refundAmountInverse = item.def["summoning_refund_amount_inverse", -1]
        val refundAmount = item.def["shard_refund_amount", -1]

        if (refundAmountInverse == -1) {
            player.message("You will receive $refundAmount shards per $itemType.")
        } else {
            player.message("You will receive 1 shard for every $refundAmountInverse $itemType.")
        }
    }

    /**
     * Opens the interface used for trading in summoning pouches and scrolls for shards.
     *
     * @param player: The [Player] to open the interface for
     * @param isPouchTradeIn: If the interface to be opened is for trading in pouches. Otherwise, the scroll
     * trade in interface is opened
     */
    fun openTradeInInterface(player: Player, isPouchTradeIn: Boolean) {
        player.open("summoning_trade_in")

        // Pouch trade in set up
        player.interfaces.sendVisibility("summoning_trade_in", "pouch_trade_in_text", isPouchTradeIn)
        player.interfaces.sendVisibility("summoning_trade_in", "pouch_trade_in", isPouchTradeIn)
        player.interfaces.sendVisibility("summoning_trade_in", "pouch_tab_scroll_bar", isPouchTradeIn)

        // Scroll trade in set up
        player.interfaces.sendVisibility("summoning_trade_in", "scroll_trade_in_text", !isPouchTradeIn)
        player.interfaces.sendVisibility("summoning_trade_in", "scroll_trade_in", !isPouchTradeIn)
        player.interfaces.sendVisibility("summoning_trade_in", "scroll_tab_scroll_bar", !isPouchTradeIn)

        val scrollTabActiveSprite = 1190
        val pouchTabActiveSprite = 1191
        val scrollTabInactiveSprite = 1192
        val pouchTabInactiveSprite = 1193
        val interfaceId = 78

        val componentId: Int
        val componentString: String
        val script: String

        if (isPouchTradeIn) {
            script = "populate_summoning_pouch_trade_in"
            componentId = 15
            componentString = "pouch_trade_in"
            player.interfaces.sendSprite("summoning_trade_in", "pouch_tab_sprite", pouchTabActiveSprite)
            player.interfaces.sendSprite("summoning_trade_in", "scroll_tab_sprite", scrollTabInactiveSprite)
        } else {
            script = "populate_summoning_scroll_trade_in"
            componentId = 14
            componentString = "scroll_trade_in"
            player.interfaces.sendSprite("summoning_trade_in", "pouch_tab_sprite", pouchTabInactiveSprite)
            player.interfaces.sendSprite("summoning_trade_in", "scroll_tab_sprite", scrollTabActiveSprite)
        }

        val width = 8
        val height = 10
        val startingIndex = 1
        val endingIndex = 78

        player.sendScript(
            script,
            InterfaceDefinition.pack(interfaceId, componentId),
            width,
            height,
            startingIndex,
            endingIndex,
            "Value<col=FF9040>",
            "Trade<col=FF9040>",
            "Trade-5<col=FF9040>",
            "Trade-10<col=FF9040>",
            "Trade-X<col=FF9040>",
            "Trade-All<col=FF9040>",
        )

        player.interfaceOptions.unlockAll("summoning_trade_in", componentString, 0..400)
    }
}
