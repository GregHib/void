package content.social.trade.exchange.history

import content.social.trade.exchange.history.ItemHistory.Companion.readHistory
import content.social.trade.exchange.history.ItemHistory.Companion.write
import content.social.trade.exchange.offer.Offer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.writePair
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.timedLoad
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Historical aggregated data on every item
 * https://web.archive.org/web/20210430192551/https://secure.runescape.com/m%3Dforum/sl%3D0/forums?98,99,806,63785618
 */
class ExchangeHistory(
    private val itemDefinitions: ItemDefinitions,
    private val history: MutableMap<String, ItemHistory> = mutableMapOf()
) {
    private val marketPrices = mutableMapOf<String, Int>()

    fun record(item: String, amount: Int, price: Int) {
        val history = history.getOrPut(item) { ItemHistory() }
        val timestamp = System.currentTimeMillis()
        history.record(timestamp, price, amount)
    }

    fun clean() {
        val timestamp = System.currentTimeMillis()
        for (item in history.values) {
            item.clean(timestamp)
        }
    }

    fun marketPrice(item: String): Int {
        val price = marketPrices[item]
        if (price == null) {
            val definition = itemDefinitions.get(item)
            return definition["price", definition.cost]
        }
        return price
    }

    fun calculatePrices() {
        val timestamp = System.currentTimeMillis()
        for ((item, history) in history) {
            val previous = marketPrices[item]
            val (time, newest) = history.day.maxByOrNull { it.key } ?: continue
            val age = timestamp - time
            if (TimeUnit.MILLISECONDS.toDays(age) <= 1) {
                continue
            }
            var next = previous ?: newest.high
            if (previous != null) {
                next += (newest.high - previous).coerceIn(-(previous / 20), previous / 20) // 5 percent max change
            }
            marketPrices[item] = next
        }
    }

    fun clear() {
        history.clear()
        marketPrices.clear()
    }

    fun save(directory: File, playerFile: File) {
        for ((key, value) in history) {
            Config.fileWriter(directory.resolve("${key}.toml")) {
                write(value)
            }
        }
    }

    fun load(directory: File, playerFile: File): ExchangeHistory {
        clear()
        timedLoad("grand exchange item history") {
            for (file in directory.listFiles()!!) {
                Config.fileReader(file) {
                    history[file.nameWithoutExtension] = readHistory()
                }
            }
            calculatePrices()
            history.size
        }
        return this
    }
}