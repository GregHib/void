package world.gregs.voidps.engine.data.exchange

/**
 * An open Grand Exchange offer
 * @param id unique
 * @param remaining number left to purchase or sell
 * @param coins total received or refunded if bought below offer price
 * @param account account name of the player
 */
data class OpenOffer(
    override val id: Int = 0,
    var remaining: Int = 0,
    override var coins: Int = 0,
    val account: String = "",
    var lastActive: Long = System.currentTimeMillis(),
) : Offer {
    val sell: Boolean
        get() = remaining < 0
}