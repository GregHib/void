package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.social.trade.exchange.history.ExchangeHistory
import content.social.trade.exchange.limit.BuyLimits
import content.social.trade.exchange.offer.ClaimableOffers
import content.social.trade.exchange.offer.PendingOffer
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.grandExchange
import world.gregs.voidps.type.random
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.ceil

class GrandExchange(
    val offers: Offers,
    private val itemDefinitions: ItemDefinitions,
    val history: ExchangeHistory,
    private val accounts: AccountDefinitions,
    private val players: Players,
    private val claims: ClaimableOffers,
    private val storage: Storage
) : Runnable {

    private val limits = BuyLimits(itemDefinitions)

    private val pending = mutableListOf<PendingOffer>()
    private val cancellations = mutableListOf<Pair<Int, String>>()

    private val logger = InlineLogger()


    /**
     * Add an offer to sell an item starting next tick
     */
    fun sell(player: Player, item: Item, price: Int): ExchangeOffer {
        val id = offers.id()
        val offer = ExchangeOffer(id, item.id, item.amount, price, OfferState.PendingSell)
        offers.add(offer)
        pending.add(PendingOffer(player.accountName, offer))
        return offer
    }

    /**
     * Add an offer to buy an item starting next tick
     */
    fun buy(player: Player, item: Item, price: Int): ExchangeOffer {
        val id = offers.id()
        val offer = ExchangeOffer(id, item.id, item.amount, price, OfferState.PendingBuy)
        offers.add(offer)
        pending.add(PendingOffer(player.accountName, offer))
        return offer
    }

    /**
     * Cancel an offer starting next tick
     */
    fun cancel(player: Player, id: Int) {
        cancellations.add(id to player.accountName)
    }

    fun login(player: Player) {
        val now = System.currentTimeMillis()
        var claimed = false
        for (slot in 0 until 6) {
            val offer = player.offers.getOrNull(slot) ?: continue
            offers.update(offer, now)
            val claim = claims.claim(offer.id) ?: continue
            claim(player, slot, offer, offer.item, claim.amount, claim.coins, notify = false)
            claimed = true
        }
        if (claimed) {
            player.message("You have items waiting in your Grand Exchange collection box!")
        }
    }

    fun clear() {
        cancellations.clear()
        pending.clear()
        offers.clear()
        claims.clear()
    }

    override fun run() {
        for ((index, account) in cancellations) {
            val def = accounts.getByAccount(account)
            val player = players.get(def?.displayName ?: "") ?: continue
            val offer = player.offers[index]
            offers.remove(offer)
            if (offer.state.cancelled) {
                continue
            }
            offer.cancel()
            returnItems(player, offer)
        }
        cancellations.clear()
        for (offer in pending) {
            process(offer)
        }
        pending.clear()
        limits.tick()
        if (GameLoop.tick % 6000 == 0) { // 1 hour
            offers.removeInactive(Settings["grandExchange.offers.activeDays", 0])
            history.clean()
            history.calculatePrices()
            save()
        }
    }

    private fun returnItems(player: Player?, offer: ExchangeOffer) {
        val remaining = offer.amount - offer.completed

        if (offer.sell) {
            if (player == null) {
                claims.add(offer.id, remaining)
                return
            }

            val slot = slot(player, offer.id)
            if (slot == -1) {
                logger.warn { "Failed to find GE sell slot: $offer" }
                claims.add(offer.id, remaining)
                return
            }
            claim(player, slot, offer, offer.item, remaining)
            refresh(player, slot)
        } else {
            if (player == null) {
                claims.add(offer.id, 0, remaining * offer.price)
                return
            }

            val slot = slot(player, offer.id)
            if (slot == -1) {
                logger.warn { "Failed to find GE buy slot: $offer" }
                claims.add(offer.id, 0, remaining * offer.price)
                return
            }
            claim(player, slot, offer, offer.item, 0, remaining * offer.price)
            refresh(player, slot)
        }
    }

    fun save() {
        storage.savePriceHistory(history.history)
        storage.saveClaims(claims.claims)
    }

    private fun process(pending: PendingOffer) {
        val offer = pending.offer
        if (offer.sell) {
            while (offer.completed < offer.amount) {
                // Find the highest buyer
                val buying = offers.buying(offer.item)
                val entry = buying.lastEntry()
                if (entry == null || entry.key < offer.price || !exchange(entry.key, entry.value, offer, pending.account)) {
                    offers.sell(pending.account, offer)
                    refresh(pending.account)
                    break
                }
            }
        } else {
            while (offer.completed < offer.amount) {
                // Find the cheapest seller
                val selling = offers.selling(offer.item)
                val entry = selling.firstEntry()
                if (entry == null || entry.key > offer.price || !exchange(entry.key, entry.value, offer, pending.account)) {
                    offers.buy(pending.account, offer)
                    refresh(pending.account)
                    break
                }
            }
        }
    }

    private fun refresh(account: String) {
        val definition = accounts.getByAccount(account) ?: return
        val player = players.get(definition.displayName) ?: return
        refresh(player)
    }

    fun refresh(player: Player) {
        for (i in 0 until 6) {
            refresh(player, i)
        }
    }

    fun refresh(player: Player, index: Int) {
        player.sendInventory("collection_box_${index}")
        val offer = player.offers.getOrNull(index)
        if (offer == null || offer.isEmpty()) {
            player.removeVarbit("grand_exchange_ranges", "slot_${index}")
            player.client?.grandExchange(index)
            return
        }
        val price = history.marketPrice(offer.item)
        if (offer.price in ceil(price * 0.95).toInt()..ceil(price * 1.05).toInt()) {
            player.removeVarbit("grand_exchange_ranges", "slot_${index}")
        } else if (Settings["grandExchange.priceLimit", true]) {
            player.addVarbit("grand_exchange_ranges", "slot_${index}")
        }
        val itemDef = itemDefinitions.get(offer.item)
        player.client?.grandExchange(index, offer.state.int, itemDef.id, offer.price, offer.amount, offer.completed, offer.coins)
    }

    private fun exchange(price: Int, traders: MutableList<OpenOffer>, offer: ExchangeOffer, account: String): Boolean {
        val trader = weightedSample(traders)
        // if offer has more or same as other
        val required = offer.amount - offer.completed
        val available = trader.remaining.absoluteValue
        var traded = if (required >= available) available else required
        traded = traded.coerceAtMost(limits.limit(if (offer.sell) trader.account else account, offer.item))

        if (traded <= 0) {
            return false
        }

        exchange(offer, account, traded)
        exchange(trader, offer.item, price, traded, !offer.state.sell)
        if (offer.sell) {
            claim(offer, account, offer.item, coins = price * traded) // best possible offer
            claim(trader, trader.account, offer.item, traded, notify = true)
        } else {
            // return excess coins from selling at lowest sell offer
            claim(offer, account, offer.item, traded, coins = (offer.price - price) * traded)
            claim(trader, trader.account, offer.item, coins = price * traded, notify = true)
        }
        limits.record(account, offer.item, traded)
        history.record(offer.item, traded, offer.price)
        return true
    }

    private fun claim(offer: Offer, account: String, item: String, amount: Int = 0, coins: Int = 0, notify: Boolean = false) {
        val definition = accounts.getByAccount(account)
        val player = players.get(definition?.displayName ?: "")
        if (player == null) {
            claims.add(offer.id, amount, coins)
            return
        }
        val slot = slot(player, offer.id)
        if (slot == -1) {
            logger.warn { "Failed to find GE claim slot: $offer $amount $coins" }
            claims.add(offer.id, amount, coins)
            return
        }
        claim(player, slot, offer, item, amount, coins, notify)
    }

    private fun slot(player: Player, id: Int): Int {
        return player.offers.indexOfFirst { it.id == id }
    }

    private fun claim(player: Player, slot: Int, offer: Offer, item: String, amount: Int, coins: Int = 0, notify: Boolean = false) {
        val inv = player.inventories.inventory("collection_box_${slot}")
        inv.transaction {
            if (amount > 0) {
                add(item, amount)
            }
            if (coins > 0) {
                add("coins", coins)
            }
        }
        if (inv.transaction.failed) {
            logger.warn { "Failed to claim GE sale: $slot $offer $amount $coins" }
            claims.add(offer.id, amount, coins)
            return
        } else if (notify && !player.hasClock("grand_exchange_message_cooldown")) {
            player.start("grand_exchange_message_cooldown", TimeUnit.MINUTES.toTicks(10))
            // https://youtu.be/3ussM7P1j00?si=IHR8ZXl2kN0bjIfx&t=398
            player.message("One or more of your Grand Exchange offers have been updated.")
        }
        offer.coins += coins
        refresh(player, slot)
    }

    private fun exchange(offer: ExchangeOffer, account: String, amount: Int) {
        offer.completed += amount
        if (offer.completed == offer.amount) {
            offer.cancel()
            offers.remove(offer)
            history.record(account, offer.id, offer.price)
        }
    }

    private fun exchange(offer: OpenOffer, item: String, price: Int, amount: Int, sell: Boolean) {
        if (sell) {
            offer.remaining += amount
        } else {
            offer.remaining -= amount
        }
        if (offer.remaining == 0) {
            offers.remove(offer.id, item, price, sell)
            history.record(offer.account, offer.id, price)
        }
    }

    /**
     * Sample the offers provided and weigh in favour of oldest offers
     */
    private fun weightedSample(offers: List<OpenOffer>): OpenOffer {
        if (offers.isEmpty()) {
            throw IllegalArgumentException("TreeMap must not be empty")
        }

        val cumulativeMap = TreeMap<Long, OpenOffer>()
        var totalWeight = 0L
        val now = System.currentTimeMillis()
        for (offer in offers) {
            val age = now - offer.lastActive
            totalWeight += age
            cumulativeMap[totalWeight] = offer
        }

        if (totalWeight <= 0) {
            throw IllegalStateException("Sampling failed due to invalid cumulative map")
        }
        val randomValue = random.nextLong(totalWeight)
        val entry = cumulativeMap.ceilingEntry(randomValue)
            ?: throw IllegalStateException("Sampling failed due to invalid cumulative map")
        return entry.value
    }

    companion object {
        fun clear(player: Player) {
            player["grand_exchange_box"] = -1
            player["grand_exchange_page"] = "offers"
            player.sendScript("item_dialogue_close")
            player.close("stock_side")
            player.close("item_info")
            player.clear("grand_exchange_item")
            player.clear("grand_exchange_item_id")
            player.clear("grand_exchange_price")
            player.clear("grand_exchange_market_price")
            player.clear("grand_exchange_range_min")
            player.clear("grand_exchange_range_max")
            player.clear("grand_exchange_quantity")
        }
    }
}