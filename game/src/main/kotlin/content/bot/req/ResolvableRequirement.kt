package content.bot.req

import content.bot.Bot

sealed interface ResolvableRequirement : Requirement

sealed interface ItemRequirement : ResolvableRequirement {
    val id: String
    val amount: Int
}

data class RequiresEquippedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement {
    fun check(bot: Bot) {
        // bot.inventory.has(id, amount)
    }
}

data class RequiresCarriedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement

data class RequiresOwnedItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemRequirement

data class RequiresInvSpace(
    val amount: Int,
) : ResolvableRequirement

data class RequiresLocation(
    val id: String,
) : ResolvableRequirement

data class RequiresTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : ResolvableRequirement