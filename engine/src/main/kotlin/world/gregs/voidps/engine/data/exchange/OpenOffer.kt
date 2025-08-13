package world.gregs.voidps.engine.data.exchange

/**
 * An open Grand Exchange offer
 * @param id unique
 * @param amount to buy or sell
 * @param completed number purchased or sold so far
 * @param coins total received or refunded if bought below offer price
 * @param lastActive last time the player logged in
 * @param account account name of the player
 */
data class OpenOffer(
    val id: Int = 0,
    val amount: Int = 0,
    var completed: Int = 0,
    var coins: Int = 0,
    var lastActive: Long = System.currentTimeMillis(),
    val account: String = "",
)