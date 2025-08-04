package content.social.trade.exchange

enum class OfferState {
    PendingBuy,
    PendingSell,
    BuyOpen,
    SellOpen,
    BuyCompleted,
    SellCompleted,
    BuyCanceled,
    SellCanceled,
    Inactive,
}