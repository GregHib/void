package content.social.trade.exchange.offer

import world.gregs.voidps.engine.data.exchange.Claim
import kotlin.collections.MutableMap
import kotlin.collections.mutableMapOf
import kotlin.collections.set

/**
 * Completed [Offers] which can be claimed by players when they log in
 */
class ClaimableOffers(
    val claims: MutableMap<Int, Claim> = mutableMapOf(),
) {

    fun add(id: Int, amount: Int, price: Int = 0) {
        claims[id] = Claim(amount, price)
    }

    fun claim(offerId: Int): Claim? = claims.remove(offerId)

    fun clear() {
        claims.clear()
    }
}
