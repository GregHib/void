package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equips
import world.gregs.voidps.engine.inv.inventory

sealed class ResolvableFact(priority: Int) : Fact(priority)

data class EquipsItem(
    val id: String,
    val amount: Int = 1,
) : ResolvableFact(100) {
    override fun check(bot: Bot) = bot.player.equips(id, amount)
}

data class CarriesItem(
    val id: String,
    val amount: Int = 1,
) : ResolvableFact(100) {
    override fun check(bot: Bot) = bot.player.carriesItem(id, amount)
}

data class EquipsOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : ResolvableFact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.equips(id, amount) }
}

data class CarriesOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : ResolvableFact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.carriesItem(id, amount) }
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