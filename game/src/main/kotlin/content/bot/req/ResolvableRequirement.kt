package content.bot.req

import content.bot.Bot

sealed class ResolvableRequirement(priority: Int) : Requirement(priority)

sealed class ItemRequirement : ResolvableRequirement(100) {
    abstract val id: String
    abstract val amount: Int
}

data class RequiresEquippedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement() {
    fun check(bot: Bot) {
        // bot.inventory.has(id, amount)
    }
}

data class RequiresCarriedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement()

data class RequiresOwnedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement()

data class RequiresInvSpace(
    val amount: Int,
) : ResolvableRequirement(10)

data class RequiresLocation(
    val id: String,
) : ResolvableRequirement(1000)

data class RequiresTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : ResolvableRequirement(1100)