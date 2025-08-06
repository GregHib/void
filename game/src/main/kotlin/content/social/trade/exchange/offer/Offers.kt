package content.social.trade.exchange.offer

import content.social.trade.exchange.offer.Offer.Companion.readOffer
import content.social.trade.exchange.offer.Offer.Companion.write
import world.gregs.config.Config
import world.gregs.config.writeSection
import java.io.File
import java.util.*
import kotlin.math.max

class Offers(
    private val sellByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    private val buyByItem: MutableMap<String, TreeMap<Int, MutableList<Offer>>> = mutableMapOf(),
    private val offers: MutableMap<Long, Offer> = mutableMapOf(),
    private var counter: Long = 0,
) {

    fun id(): Long = ++counter

    fun add(offer: Offer) {
        offers[offer.id] = offer
    }

    fun buy(offer: Offer) {
        buyByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(offer)
    }

    fun sell(offer: Offer) {
        sellByItem.getOrPut(offer.item) { TreeMap() }.getOrPut(offer.price) { mutableListOf() }.add(offer)
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

    fun load(buyDirectory: File, sellDirectory: File) {
        load(buyDirectory, buyByItem, false)
        load(sellDirectory, sellByItem, true)
    }

    private fun load(buyDirectory: File, map: MutableMap<String, TreeMap<Int, MutableList<Offer>>>, sell: Boolean) {
        val files = buyDirectory.listFiles { _, name -> name.endsWith(".toml") } ?: return
        for (file in files) {
            val tree = TreeMap<Int, MutableList<Offer>>()
            val item = file.nameWithoutExtension
            Config.fileReader(file) {
                while (nextSection()) {
                    val id = section().toLong()
                    val offer = readOffer(item, sell)
                    counter = max(counter, id)
                    tree.getOrPut(offer.price) { mutableListOf() }.add(offer)
                    offers[id] = offer
                    map[offer.item]?.get(offer.price)?.add(offer)
                }
            }
            map[item] = tree
        }
    }
}
