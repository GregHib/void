package world.gregs.voidps.engine.data.exchange

import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

/**
 * @param sellByItem list of all [OfferState.OpenSell] [OpenOffer]'s grouped by id and price
 * @param buyByItem list of all [OfferState.OpenBuy] [OpenOffer]'s grouped by id and price
 * @param activity list of all Grand Exchange [OpenOffer]'s
 * @param counter accumulating id
 */
class OpenOffers(
    internal val sellByItem: MutableMap<String, TreeMap<Int, MutableList<OpenOffer>>> = mutableMapOf(),
    internal val buyByItem: MutableMap<String, TreeMap<Int, MutableList<OpenOffer>>> = mutableMapOf(),
    private val activity: SortedSet<Activity> = TreeSet(Comparator { a1, a2 -> a1.lastActive.compareTo(a2.lastActive) }),
    var counter: Int = 0,
) {

    data class Activity(val item: String, val price: Int, val id: Int, var lastActive: Long = System.currentTimeMillis())

    fun id(): Int = ++counter

    fun removeInactive(days: Int) {
        if (days <= 0) {
            return
        }
        val now = System.currentTimeMillis()
        val it = activity.iterator()
        while (it.hasNext()) {
            val activity = it.next()
            val age = now - activity.lastActive
            if (TimeUnit.MILLISECONDS.toDays(age) <= days) {
                break
            }
            it.remove()
            remove(activity.id, activity.item, activity.price.absoluteValue, activity.price < 0)
        }
    }

    fun active(offer: ExchangeOffer): Boolean = (if (offer.sell) selling(offer.item) else buying(offer.item))[offer.price]?.any { it.id == offer.id } ?: false

    fun add(offer: ExchangeOffer) {
        add(offer.id, offer.item, offer.price, offer.state.sell)
    }

    fun add(id: Int, item: String, price: Int, sell: Boolean) {
        activity.add(Activity(item, if (sell) -price else price, id))
    }

    fun buy(account: String, offer: ExchangeOffer): OpenOffer {
        val open = offer.open(account)
        buyByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(open)
        return open
    }

    fun sell(account: String, offer: ExchangeOffer): OpenOffer {
        val open = offer.open(account)
        sellByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(open)
        return open
    }

    fun update(offer: ExchangeOffer, now: Long) {
        val byItem = if (offer.sell) sellByItem else buyByItem
        val open = byItem[offer.item]?.get(offer.price)?.firstOrNull { it.id == offer.id } ?: return
        open.lastActive = now
    }

    fun selling(item: String): TreeMap<Int, MutableList<OpenOffer>> = sellByItem[item] ?: TreeMap()

    fun buying(item: String): TreeMap<Int, MutableList<OpenOffer>> = buyByItem[item] ?: TreeMap()

    fun remove(offer: ExchangeOffer) {
        remove(offer.id, offer.item, offer.price, offer.state.sell)
    }

    fun remove(id: Int, item: String, price: Int, sell: Boolean) {
        activity.removeIf { it.id == id }
        remove(sell, id, item, price)
    }

    private fun remove(sell: Boolean, id: Int, item: String, price: Int) {
        val offers = if (sell) sellByItem else buyByItem
        val map = offers[item] ?: return
        val list = map[price] ?: return
        list.removeIf { it.id == id }
        if (list.isEmpty()) {
            map.remove(price)
        }
        if (map.isEmpty()) {
            offers.remove(item)
        }
    }

    fun clear() {
        counter = 0
        activity.clear()
        buyByItem.clear()
        sellByItem.clear()
    }
}
