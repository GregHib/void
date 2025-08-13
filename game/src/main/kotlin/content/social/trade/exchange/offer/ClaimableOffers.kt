package content.social.trade.exchange.offer

import world.gregs.voidps.engine.data.exchange.Claim
import kotlin.collections.MutableMap
import kotlin.collections.mutableMapOf
import kotlin.collections.set

/**
 * Completed [Offers] which can be claimed by players when they log in
 */
class ClaimableOffers(
    val claims: MutableMap<Int, Claim> = mutableMapOf()
) {

    fun add(id: Int, amount: Int, coins: Int = 0) {
        claims[id] = Claim(amount, coins)
    }

    fun claim(offerId: Int): Claim? {
        return claims.remove(offerId)
    }

    fun clear() {
        claims.clear()
    }

}