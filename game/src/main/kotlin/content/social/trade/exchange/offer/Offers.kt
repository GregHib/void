package content.social.trade.exchange.offer

import content.social.trade.exchange.offer.Offer.Companion.readOffer
import content.social.trade.exchange.offer.Offer.Companion.write
import world.gregs.config.Config
import world.gregs.config.writeSection
import world.gregs.voidps.engine.timedLoad
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * @param sellByItem list of all [OfferState.OpenSell] [Offer]'s grouped by id and price
 * @param buyByItem list of all [OfferState.OpenBuy] [Offer]'s grouped by id and price
 * @param offers list of all Grand Exchange [Offer]'s
 * @param counter accumulating id
 */
class Offers(
    private val sellByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    private val buyByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    private val offers: MutableMap<Int, Offer> = mutableMapOf(),
    private var counter: Int = 0,
) {

    fun id(): Int = ++counter

    fun removeInactive(days: Int) {
        if (days <= 0) {
            return
        }
        val now = System.currentTimeMillis()
        for ((_, offer) in offers) {
            val age = now - offer.lastActive
            if (TimeUnit.MILLISECONDS.toDays(age) > days) {
                remove(if (offer.sell) sellByItem else buyByItem, offer)
            }
        }
    }

    fun add(offer: Offer) {
        offers[offer.id] = offer
    }

    fun buy(offer: Offer) {
        offer.open()
        buyByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(offer)
    }

    fun sell(offer: Offer) {
        offer.open()
        sellByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(offer)
    }

    fun offer(id: Int): Offer? {
        return offers[id]
    }

    fun selling(item: String): TreeMap<Int, MutableList<Offer>> {
        return sellByItem[item] ?: TreeMap()
    }

    fun buying(item: String): TreeMap<Int, MutableList<Offer>> {
        return buyByItem[item] ?: TreeMap()
    }

    fun remove(id: Int): Offer? {
        val offer = offers[id] ?: return null
        remove(if (offer.sell) sellByItem else buyByItem, offer)
        return offer
    }

    private fun remove(offers: MutableMap<String, TreeMap<Int, MutableList<Offer>>>, offer: Offer) {
        val map = offers[offer.item] ?: return
        val list = map[offer.price] ?: return
        list.remove(offer)
        if (list.isEmpty()) {
            map.remove(offer.price)
        }
        if (map.isEmpty()) {
            offers.remove(offer.item)
        }
    }

    fun clear() {
        counter = 0
        offers.clear()
        buyByItem.clear()
        sellByItem.clear()
    }

}
