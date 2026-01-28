package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equips
import world.gregs.voidps.engine.inv.inventory

sealed class ResolvableFact(priority: Int) : Fact(priority)

sealed class ItemFact : ResolvableFact(100) {
    abstract val id: String
    abstract val amount: Int
}

data class EquipsItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemFact() {
    override fun check(bot: Bot) = bot.player.equips(id, amount)
}

data class CarriesItem(
    override val id: String,
    override val amount: Int = 1,
) : ItemFact() {
    override fun check(bot: Bot) = bot.player.carriesItem(id, amount)
}

data class HasInventorySpace(
    val amount: Int,
) : ResolvableFact(10) {
    override fun check(bot: Bot) = bot.player.inventory.spaces >= amount
}

data class AtLocation(
    val id: String,
) : ResolvableFact(1000) {
    override fun check(bot: Bot) = bot.player.tile in Areas[id]
}

data class AtTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : ResolvableFact(1100) {
    override fun check(bot: Bot) = bot.player.tile.within(x, y, radius, level)
}