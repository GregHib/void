package content.social.trade.exchange

import content.social.trade.exchange.history.ExchangeHistory
import content.social.trade.exchange.limit.BuyLimits
import content.social.trade.exchange.offer.Offer
import content.social.trade.exchange.offer.OfferState
import content.social.trade.exchange.offer.OfferType
import content.social.trade.exchange.offer.Offers
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.grandExchange
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.random.Random

class GrandExchange(
    val offers: Offers,
    private val itemDefinitions: ItemDefinitions,
    val history: ExchangeHistory,
    private val accounts: AccountDefinitions,
    private val players: Players,
    private val historyDirectory: File,
    private val buyOffersDirectory: File,
    private val sellOffersDirectory: File,
) : Runnable {

    private val limits = BuyLimits(itemDefinitions)

    private val pending = mutableListOf<Offer>()
    private val cancellations = mutableListOf<Int>()

    /**
     * Add an offer to sell an item starting next tick
     */
    fun sell(player: Player, item: Item, price: Int): Int {
        val id = offers.id()
        val offer = Offer(id, item.id, item.amount, price, sell = true, account = player.accountName)
        offers.add(offer)
        pending.add(offer)
        return id
    }

    /**
     * Add an offer to buy an item starting next tick
     */
    fun buy(player: Player, item: Item, price: Int): Int {
        val id = offers.id()
        val offer = Offer(id, item.id, item.amount, price, sell = false, account = player.accountName)
        offers.add(offer)
        pending.add(offer)
        return id
    }

    /**
     * Cancel an offer starting next tick
     */
    fun cancel(id: Int) {
        cancellations.add(id)
    }

    fun refresh(id: Int) {
        offers.offer(id)?.lastActive = System.currentTimeMillis()
    }

    override fun run() {
        for (id in cancellations) {
            val offer = offers.remove(id) ?: continue
            offer.state = OfferState.Cancelled
        }
        cancellations.clear()
        for (offer in pending) {
            process(offer)
        }
        pending.clear()
        limits.tick()
        if (GameLoop.tick % 6000 == 0) { // 1 hour
            history.clean()
            history.calculatePrices()
            save()
        }
    }

    fun save() {
        history.save(historyDirectory)
        offers.save(buyOffersDirectory, sellOffersDirectory)
    }

    private fun process(offer: Offer) {
        if (offer.sell) {
            while (offer.remaining < offer.amount) {
                // Find the highest buyer
                val entry = offers.buying(offer.item).ceilingEntry(offer.price)
                if (entry == null) {
                    offers.sell(offer)
                    refresh(offer.account)
                    return
                }
                exchange(entry.value, offer, buy = false)
            }
        } else {
            while (offer.remaining < offer.amount) {
                // Find the cheapest seller
                val entry = offers.selling(offer.item).floorEntry(offer.price)
                if (entry == null) {
                    offers.buy(offer)
                    refresh(offer.account)
                    break
                }
                exchange(entry.value, offer, buy = true)
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
        val id: Int = player["grand_exchange_offer_${index}"] ?: return
        val offer = this.offers.offer(id)
        if (offer == null) {
            player.removeVarbit("grand_exchange_ranges", "slot_${index}")
            player.client?.grandExchange(index)
            return
        }
        val price = history.marketPrice(offer.item)
        if (offer.price in ceil(price * 0.95).toInt()..ceil(price * 1.05).toInt()) {
            player.removeVarbit("grand_exchange_ranges", "slot_${index}")
        } else {
            player.addVarbit("grand_exchange_ranges", "slot_${index}")
        }
        val itemDef = itemDefinitions.get(offer.item)
        val state = when (offer.state) {
            OfferState.Pending -> 1
            OfferState.Open -> 2
            OfferState.Completed -> 3
            OfferState.Cancelled -> 5
        }
        val inv = player.inventories.inventory("collection_box_${index}")
        inv.transaction {
            if (offer.remaining > 0) {
                add(offer.item, offer.remaining)
            }
            if (offer.excess > 0) {
                add("coins", offer.excess)
            }
        }
        player.client?.grandExchange(index, state, itemDef.id, offer.price, offer.amount, offer.remaining, offer.excess)
    }

    private fun exchange(traders: MutableList<Offer>, offer: Offer, buy: Boolean) {
        val trader = weightedSample(traders)
        // if offer has more or same as other
        var traded = if (offer.remaining >= trader.remaining) trader.remaining else offer.remaining
        if (buy) {
            traded = traded.coerceAtMost(limits.limit(offer.account, offer.item))
        }

        offer.remaining -= traded
        trader.remaining -= traded
        trader.lastUpdated = System.currentTimeMillis()
        // Return excess coins
        offer.excess += if (buy) {
            (offer.price - trader.price) * traded
        } else {
            trader.price * traded
        }

        if (offer.remaining >= traded) {
            trader.state = OfferState.Completed
        } else {
            offer.state = OfferState.Completed
        }

        limits.record(offer.account, offer.item, traded)
        history.record(offer.item, traded, offer.price)
        notify(trader.account)
        refresh(trader.account)
        refresh(offer.account)
    }

    private fun notify(account: String) {
        val definition = accounts.getByAccount(account) ?: return
        val player = players.get(definition.displayName) ?: return
        if (player.hasClock("grand_exchange_message_cooldown")) {
            return
        }
        player.start("grand_exchange_message_cooldown", TimeUnit.MINUTES.toTicks(10))
        player.message("One or more of your Grand Exchange offers have been updated.")
    }

    /**
     * Sample the offers provided and weigh in favour of oldest offers
     */
    private fun weightedSample(offers: List<Offer>): Offer {
        if (offers.isEmpty()) {
            throw IllegalArgumentException("TreeMap must not be empty")
        }

        val cumulativeMap = TreeMap<Long, Offer>()
        var totalWeight = 0L
        val now = System.currentTimeMillis()
        for (offer in offers) {
            val age = now - offer.lastActive
            if (offer.state == OfferState.Cancelled || offer.state == OfferState.Completed) {
                continue
            }
            if (TimeUnit.MILLISECONDS.toDays(age) > 7) {
                continue // Inactive
            }
            totalWeight += age
            cumulativeMap[totalWeight] = offer
        }

        val randomValue = Random.nextLong(totalWeight)
        val entry = cumulativeMap.ceilingEntry(randomValue)
            ?: throw IllegalStateException("Sampling failed due to invalid cumulative map")
        return entry.value
    }
}