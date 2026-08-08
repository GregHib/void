package content.social.trade.monitor

import kotlin.math.abs

/**
 * Detects grand exchange trades executing far from market price -
 * sudden crashes or spikes often signal duped goods flooding the market.
 */
object PriceAnomaly {

    fun deviation(price: Int, market: Int): Double {
        if (market <= 0) {
            return 0.0
        }
        return abs(price - market) / market.toDouble()
    }

    fun flagged(price: Int, amount: Int, market: Int, minValue: Long, threshold: Double): Boolean {
        if (price.toLong() * amount < minValue) {
            return false
        }
        return deviation(price, market) >= threshold
    }
}
