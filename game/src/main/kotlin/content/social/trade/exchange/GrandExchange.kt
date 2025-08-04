package content.social.trade.exchange

import world.gregs.voidps.engine.entity.item.Item
import java.util.*
import kotlin.random.Random

class GrandExchange(
    val offers: Offers,
) {


    fun instantBuy() {

    }

    fun instantSell() {

    }

    private val pending = mutableListOf<Offer>()

    fun tick() {
        for (offer in pending) {

        }
        pending.clear()
        recalculatePrices()
    }

    fun process(offer: Offer) {
        when (offer.type) {
            OfferType.Buy -> {
                while (offer.remaining < offer.quantity) {
                    // Find the cheapest seller
                    val entry = offers.selling(offer.item).floorEntry(offer.price)
                    if (entry == null) {
                        offers.buy(offer)
                        break
                    }
                    val seller = weightedSample(entry.value)
                    // if buying more or same as seller has
                    val sold = if (offer.remaining < seller.remaining) offer.remaining else seller.remaining

                    offer.remaining -= sold
                    seller.remaining -= sold
                    // Return excess coins to buyer
                    offer.excess += (offer.price - seller.price) * sold
                    // TODO message player if logged in

                    if (offer.remaining >= seller.remaining) {
                        seller.state = OfferState.SellCompleted
                    } else { // buying less than seller has
                        offer.state = OfferState.BuyCompleted
                        break
                    }
                }
            }
            OfferType.Sell -> {
                while (offer.remaining < offer.quantity) {
                    // Find the highest buyer
                    val entry = offers.buying(offer.item).ceilingEntry(offer.price)
                    if (entry == null) {
                        offers.sell(offer)
                        return
                    }
                    val buyer = weightedSample(entry.value)
                    // if selling less or same as buyer has
                    val sold = if (offer.remaining > buyer.remaining) buyer.remaining else offer.remaining

                    offer.remaining -= sold
                    buyer.remaining -= sold
                    // Set coins given to buyer
                    offer.excess += buyer.price * sold

                    if (offer.remaining >= buyer.remaining) {
                        buyer.state = OfferState.BuyCompleted
                    } else { // selling less than buyer needs
                        offer.state = OfferState.SellCompleted
                        break
                    }
                }

            }
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
            totalWeight += age
            cumulativeMap[totalWeight] = offer
        }

        val randomValue = Random.nextLong(totalWeight)
        val entry = cumulativeMap.ceilingEntry(randomValue)
            ?: throw IllegalStateException("Sampling failed due to invalid cumulative map")
        return entry.value
    }

    fun recalculatePrices() {

    }

    fun sellOffers(id: String) {

    }

    fun buyOffers(id: String) {

    }

    fun sell(item: Item, price: Int): Long {
        val offer = Offer(OfferType.Sell, item.id, item.amount, price)
        val id = offers.add(offer)
        pending.add(offer)
        return id
    }

    fun buy(item: Item, price: Int): Long {
        val offer = Offer(OfferType.Sell, item.id, item.amount, price)
        val id = offers.add(offer)
        pending.add(offer)
        return id
    }

    fun cancel(slot: Int) {

    }
}