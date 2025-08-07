package content.social.trade.exchange.offer

import world.gregs.config.*
import java.io.File

class UncollectedOffers(
    private val claims: MutableMap<Int, Claim> = mutableMapOf()
) {

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
                        0 -> writeValue(claim.item)
                        1 -> writeValue(claim.amount)
                        2 -> writeValue(claim.coins)
                    }
                }
            }
        }
    }

    fun load(file: File): UncollectedOffers {
        Config.fileReader(file) {
            while (nextPair()) {
                val id = key().toInt()
                assert(nextElement())
                val item = string()
                assert(nextElement())
                val amount = int()
                assert(nextElement())
                val coins = int()
                claims[id] = Claim(item = item, amount = amount, coins = coins)
            }
        }
        return this
    }
}