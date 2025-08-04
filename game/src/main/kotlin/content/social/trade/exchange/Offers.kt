package content.social.trade.exchange

import java.util.*

class Offers(
    val sellByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    val buyByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    val offers: MutableMap<Long, Offer> = mutableMapOf(),
    var counter: Long = 0,
) {

    fun add(offer: Offer): Long {
        val id = counter++
        offers[id] = offer
        return id
    }

    fun buy(offer: Offer) {
        buyByItem[offer.item]?.get(offer.price)?.add(offer)
    }

    fun sell(offer: Offer) {
        sellByItem[offer.item]?.get(offer.price)?.add(offer)
    }

    fun offer(id: Long): Offer? {
        return offers[id]
    }

    fun selling(item: String): TreeMap<Int, MutableList<Offer>> {
        return sellByItem[item] ?: TreeMap()
    }

    fun buying(item: String): TreeMap<Int, MutableList<Offer>> {
        return buyByItem[item] ?: TreeMap()
    }

    fun remove(id: Long): Offer? {
        val offer = offers[id] ?: return null
        if (offer.type == OfferType.Buy) {
            buyByItem[offer.item]?.get(offer.price)?.remove(offer)
        } else if (offer.type == OfferType.Sell) {
            sellByItem[offer.item]?.get(offer.price)?.remove(offer)
        }
        return offer
    }
}