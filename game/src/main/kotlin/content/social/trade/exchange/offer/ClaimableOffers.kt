package content.social.trade.exchange.offer

import world.gregs.config.*
import java.io.File

class ClaimableOffers(
    private val claims: MutableMap<Int, Claim> = mutableMapOf()
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

    fun save(file: File) {
        Config.fileWriter(file) {
            for ((id, claim) in claims) {
                writeKey(id.toString())
                list(3) { index ->
                    when (index) {
                        0 -> writeValue(claim.amount)
                        1 -> writeValue(claim.coins)
                    }
                }
            }
        }
    }

    fun load(file: File): ClaimableOffers {
        if (!file.exists()) {
            return this
        }
        Config.fileReader(file) {
            while (nextPair()) {
                val id = key().toInt()
                assert(nextElement())
                val amount = int()
                assert(nextElement())
                val coins = int()
                claims[id] = Claim(amount = amount, coins = coins)
            }
        }
        return this
    }
}