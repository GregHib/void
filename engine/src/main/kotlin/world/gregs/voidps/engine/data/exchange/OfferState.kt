package world.gregs.voidps.engine.data.exchange

enum class OfferState(val int: Int) {
    PendingBuy(1),
    PendingSell(9),
    OpenBuy(2),
    OpenSell(10),
    CompletedBuy(5),
    CompletedSell(13),
    ;

    val open: Boolean
        get() = this == OpenBuy || this == OpenSell

    val sell: Boolean
        get() = int and 0x8 == 8

    val cancelled: Boolean
        get() = int.rem(0x8) == 5
}
