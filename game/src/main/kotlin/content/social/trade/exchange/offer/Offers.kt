package content.social.trade.exchange.offer

import content.social.trade.exchange.offer.Offer.Companion.readOffer
import content.social.trade.exchange.offer.Offer.Companion.write
import world.gregs.config.Config
import world.gregs.config.writePair
import world.gregs.config.writeSection
import java.io.File
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
        if (offer.sell) {
            sellByItem[offer.item]?.get(offer.price)?.remove(offer)
        } else {
            buyByItem[offer.item]?.get(offer.price)?.remove(offer)
        }
        return offer
    }

    fun clear() {
        counter = 0
        offers.clear()
        buyByItem.clear()
        sellByItem.clear()
    }

    fun save(file: File) {
        Config.fileWriter(file) {
            writeSection("offers")
            writePair("count", counter)
            for ((id, offer) in offers) {
                writeSection(id.toString())
                write(offer)
            }
        }
    }

    fun load(file: File) {
        Config.fileReader(file) {
            val section = section()
            assert(section == "offers")
            counter = long()
            while (nextSection()) {
                val id = long()
                val offer = readOffer()
                offers[id] = offer
                if (offer.sell) {
                    sell(offer)
                } else {
                    buy(offer)
                }
            }
        }
    }
}
