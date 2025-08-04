package content.social.trade.exchange

import java.util.*

class Offers(
    val sellByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    val buyByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    val offers: MutableMap<Long, Offer> = mutableMapOf(),
    var counter: Long = 0,
) {

    fun offer(id: Long): Offer {
        return offers[id] ?: Offer.EMPTY
    }

    fun selling(item: String): TreeMap<Int, MutableList<Offer>> {
        return sellByItem[item] ?: TreeMap()
    }

    fun buying(item: String): TreeMap<Int, MutableList<Offer>> {
        return buyByItem[item] ?: TreeMap()
    }

    fun add(offer: Offer): Long {
        val id = counter++
        offers[id] = offer
        return id
    }

//    fun add(item: Item, price: Int): Long {
//        val offer = Offer(item.id, item.amount, price)
//        val id = counter++
//        offers[id] = offer
//        return id
//    }

    fun remove(id: Long) {
        val offer = offers[id] ?: return
        if (offer.state == OfferState.BuyOpen) {
            buyByItem[offer.item]?.get(offer.price)?.remove(offer)
        } else if (offer.state == OfferState.SellOpen) {
            sellByItem[offer.item]?.get(offer.price)?.remove(offer)
        }
    }

    fun buy(offer: Offer) {
        buyByItem[offer.item]?.get(offer.price)?.add(offer)
    }

    fun sell(offer: Offer) {
        sellByItem[offer.item]?.get(offer.price)?.add(offer)
    }
}