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

    fun save(buyDirectory: File, sellDirectory: File) {
        save(buyDirectory, buyByItem)
        save(sellDirectory, sellByItem)
    }

    private fun save(directory: File, map: Map<String, TreeMap<Int, MutableList<Offer>>>) {
        for ((item, tree) in map) {
            val file = directory.resolve("${item}.toml")
            Config.fileWriter(file) {
                for (offers in tree.values) {
                    for (offer in offers) {
                        writeSection(offer.id.toString())
                        write(offer)
                    }
                }
            }
        }
    }

    fun load(buyDirectory: File, sellDirectory: File, days: Int): Offers {
        timedLoad("grand exchange offer") {
            load(buyDirectory, buyByItem, days, false)
            load(sellDirectory, sellByItem, days, true)
            offers.size
        }
        return this
    }

    private fun load(buyDirectory: File, map: MutableMap<String, TreeMap<Int, MutableList<Offer>>>, days: Int, sell: Boolean) {
        val files = buyDirectory.listFiles { _, name -> name.endsWith(".toml") } ?: return
        val now = System.currentTimeMillis()
        for (file in files) {
            val tree = TreeMap<Int, MutableList<Offer>>()
            val item = file.nameWithoutExtension
            Config.fileReader(file) {
                while (nextSection()) {
                    val id = section().toInt()
                    val offer = readOffer(id, item, sell)
                    counter = max(counter, id)
                    offers[id] = offer
                    // Only store active offers
                    if (!offer.state.cancelled && (days <= 0 || TimeUnit.MILLISECONDS.toDays(now - offer.lastActive) <= days)) {
                        tree.getOrPut(offer.price) { mutableListOf() }.add(offer)
                    }
                }
            }
            map[item] = tree
        }
    }
}
