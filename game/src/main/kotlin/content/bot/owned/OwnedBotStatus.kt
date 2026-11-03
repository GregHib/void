package content.bot.owned

/**
 * What an owned bot is doing, as the owner's client variables show it. Ordinal is the wire value.
 */
enum class OwnedBotStatus {
    None,
    Idle,
    Working,
    Banking,
    Following,
    Fighting,
    Low,
    Fled,
    Failed,
    Dead,
    Guarding,
}
