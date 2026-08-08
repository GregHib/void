package content.social.trade.monitor

import content.social.trade.exchange.history.ExchangeHistory
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Flags grand exchange trades that deviate too far from market price.
 * Runs per matched exchange on the game thread; writes to [AuditLog] only.
 */
class PriceAnomalyMonitor(private val history: ExchangeHistory) : Script {

    private val flagged = mutableMapOf<String, Int>()

    init {
        history.listeners.add(::exchanged)
    }

    private fun exchanged(item: String, amount: Int, price: Int) {
        if (!Settings["economy.monitor.enabled", false] || !Settings["economy.monitor.price.enabled", false]) {
            return
        }
        val market = history.marketPrice(item)
        val minValue = Settings["economy.monitor.price.minValue", 100_000L]
        val threshold = Settings["economy.monitor.price.deviation", 0.3]
        if (!PriceAnomaly.flagged(price, amount, market, minValue, threshold)) {
            return
        }
        val cooldown = TimeUnit.MINUTES.toTicks(Settings["economy.monitor.price.cooldownMinutes", 10])
        val last = flagged[item]
        if (last != null && GameLoop.tick - last < cooldown) {
            return
        }
        flagged[item] = GameLoop.tick
        val deviation = PriceAnomaly.deviation(price, market)
        AuditLog.info("PRICE_ANOMALY\t$item\tprice=$price\tmarket=$market\tamount=$amount\tdeviation=${"%.3f".format(deviation)}")
        EconomySink.flag("price_anomaly", item = item, value = price.toLong(), details = "market=$market amount=$amount deviation=${"%.3f".format(deviation)}")
    }
}
