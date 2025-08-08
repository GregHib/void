package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.social.trade.exchange.history.ExchangeHistory
import content.social.trade.exchange.limit.BuyLimits
import content.social.trade.exchange.offer.ClaimableOffers
import content.social.trade.exchange.offer.Offer
import content.social.trade.exchange.offer.OfferState
import content.social.trade.exchange.offer.Offers
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.sendInventory
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
    private val claims: ClaimableOffers,
    private val historyDirectory: File,
    private val buyOffersDirectory: File,
    private val sellOffersDirectory: File,
    private val claimsDirectory: File
) : Runnable {

    private val limits = BuyLimits(itemDefinitions)

    private val pending = mutableListOf<Offer>()
    private val cancellations = mutableListOf<Int>()

    private val logger = InlineLogger()

    /**
     * Add an offer to sell an item starting next tick
     */
    fun sell(player: Player, item: Item, price: Int): Int {
        val id = offers.id()
        val offer = Offer(id, item.id, item.amount, price, OfferState.PendingSell, account = player.accountName)
        offers.add(offer)
        pending.add(offer)
        return id
    }

    /**
     * Add an offer to buy an item starting next tick
     */
    fun buy(player: Player, item: Item, price: Int): Int {
        val id = offers.id()
        val offer = Offer(id, item.id, item.amount, price, OfferState.PendingBuy, account = player.accountName)
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

    fun login(player: Player) {
        val now = System.currentTimeMillis()
        for (slot in 0 until 6) {
            val id: Int = player["grand_exchange_offer_${slot}"] ?: continue
            val offer = offers.offer(id) ?: continue
            offer.lastActive = now
            val claim = claims.claim(id) ?: continue
            claim(player, slot, offer, claim.amount, claim.coins, notify = true)
        }
    }

    override fun run() {
        for (id in cancellations) {
            val offer = offers.remove(id) ?: continue
            offer.cancel()
            returnItems(offer)
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

    private fun returnItems(offer: Offer) {
        val definition = accounts.getByAccount(offer.account)
        val player = players.get(definition?.displayName ?: "")
        val remaining = offer.amount - offer.completed

        if (offer.sell) {
            if (player == null) {
                claims.add(offer.id, remaining)
                return
            }

            val slot = slot(player, offer)
            if (slot == -1) {
                logger.warn { "Failed to find GE sell slot: $offer" }
                claims.add(offer.id, remaining)
                return
            }
            claim(player, slot, offer, remaining)
            refresh(player, slot)
        } else {
            if (player == null) {
                claims.add(offer.id, 0, remaining * offer.price)
                return
            }

            val slot = slot(player, offer)
            if (slot == -1) {
                logger.warn { "Failed to find GE buy slot: $offer" }
                claims.add(offer.id, 0, remaining * offer.price)
                return
            }
            claim(player, slot, offer, 0, remaining * offer.price)
            refresh(player, slot)
        }
    }

    fun save() {
        history.save(historyDirectory)
        offers.save(buyOffersDirectory, sellOffersDirectory)
        claims.save(claimsDirectory)
    }

    private fun process(offer: Offer) {
        if (offer.sell) {
            while (offer.completed < offer.amount) {
                // Find the highest buyer
                val buying = offers.buying(offer.item)
                val entry = buying.ceilingEntry(offer.price)
                if (entry == null) {
                    offers.sell(offer)
                    refresh(offer.account)
                    return
                }
                exchange(entry.value, offer)
            }
        } else {
            while (offer.completed < offer.amount) {
                // Find the cheapest seller
                val selling = offers.selling(offer.item)
                val entry = selling.floorEntry(offer.price)
                if (entry == null) {
                    offers.buy(offer)
                    refresh(offer.account)
                    break
                }
                exchange(entry.value, offer)
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
        val id: Int? = player["grand_exchange_offer_${index}"]
        if (id == null) {
            player.removeVarbit("grand_exchange_ranges", "slot_${index}")
            player.client?.grandExchange(index)
            return
        }
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
        player.client?.grandExchange(index, offer.state.int, itemDef.id, offer.price, offer.amount, offer.completed, offer.excess)
    }

    private fun exchange(traders: MutableList<Offer>, offer: Offer) {
        val trader = weightedSample(traders)
        // if offer has more or same as other
        val required = offer.amount - offer.completed
        val available = trader.amount - trader.completed
        var traded = if (required >= available) available else required
        traded = traded.coerceAtMost(limits.limit(if (offer.sell) trader.account else offer.account, offer.item))

        if (traded <= 0) {
            return
        }

        exchange(offer, traded)
        exchange(trader, traded)
        if (offer.sell) {
            claim(offer, coins = trader.price * traded) // best possible offer
            claim(trader, traded, notify = true)
        } else {
            // return excess coins from selling at lowest sell offer
            claim(offer, traded, coins = (offer.price - trader.price) * traded)
            claim(trader, coins = trader.price * traded, notify = true)
        }
        limits.record(offer.account, offer.item, traded)
        history.record(offer.item, traded, offer.price)
    }

    private fun claim(offer: Offer, amount: Int = 0, coins: Int = 0, notify: Boolean = false) {
        val definition = accounts.getByAccount(offer.account)
        val player = players.get(definition?.displayName ?: "")
        if (player == null) {
            claims.add(offer.id, amount, coins)
            return
        }
        val slot = slot(player, offer)
        if (slot == -1) {
            logger.warn { "Failed to find GE claim slot: $offer $amount $coins" }
            claims.add(offer.id, amount, coins)
            return
        }
        claim(player, slot, offer, amount, coins, notify)
    }

    private fun slot(player: Player, offer: Offer): Int {
        var slot = -1
        for (i in 0 until 6) {
            if (player["grand_exchange_offer_${i}", -1] == offer.id) {
                slot = i
                break
            }
        }
        return slot
    }

    private fun claim(player: Player, slot: Int, offer: Offer, amount: Int, coins: Int = 0, notify: Boolean = false) {
        val inv = player.inventories.inventory("collection_box_${slot}")
        inv.transaction {
            if (amount > 0) {
                add(offer.item, amount)
            }
            if (coins > 0) {
                add("coins", coins)
            }
        }
        if (inv.transaction.failed) {
            logger.warn { "Failed to claim GE sale: $slot $offer $amount $coins" }
            claims.add(offer.id, amount, coins)
            return
        } else if (notify) {
            if (player.hasClock("grand_exchange_message_cooldown")) {
                return
            }
            player.start("grand_exchange_message_cooldown", TimeUnit.MINUTES.toTicks(10))
            player.message("One or more of your Grand Exchange offers have been updated.")
        }
        refresh(player, slot)
    }

    private fun exchange(offer: Offer, amount: Int) {
        offer.completed += amount
        if (offer.completed == offer.amount) {
            offer.cancel()
            offers.remove(offer.id)
        } else {
            offer.open()
        }
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
            if (offer.state.cancelled) {
                continue
            }
            totalWeight += age
            cumulativeMap[totalWeight] = offer
        }

        if (totalWeight <= 0) {
            throw IllegalStateException("Sampling failed due to invalid cumulative map")
        }
        val randomValue = Random.nextLong(totalWeight)
        val entry = cumulativeMap.ceilingEntry(randomValue)
            ?: throw IllegalStateException("Sampling failed due to invalid cumulative map")
        return entry.value
    }
}