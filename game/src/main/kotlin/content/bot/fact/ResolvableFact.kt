package content.bot.fact

import content.bot.Bot

sealed class ResolvableFact(priority: Int) : Fact(priority)

sealed class ItemFact : ResolvableFact(100) {
    abstract val id: String
    abstract val amount: Int
}

data class EquipsItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemFact() {
    fun check(bot: Bot) {
        // bot.inventory.has(id, amount)
    }
}

data class CarriesItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemFact()

data class OwnsItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemFact()

data class HasInventorySpace(
    val amount: Int,
) : ResolvableFact(10)

data class AtLocation(
    val id: String,
) : ResolvableFact(1000)

data class AtTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : ResolvableFact(1100)