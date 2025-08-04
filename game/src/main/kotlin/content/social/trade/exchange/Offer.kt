package content.social.trade.exchange

data class Offer(
    val type: OfferType = OfferType.Buy,
    val item: String = "",
    val quantity: Int = 0,
    val price: Int = 0,
    var state: OfferState = OfferState.PendingBuy,
    var lastActive: Long = System.currentTimeMillis(),
    var remaining: Int = quantity,
    var excess: Int = 0
) {

    companion object {
        val EMPTY = Offer()
    }
}