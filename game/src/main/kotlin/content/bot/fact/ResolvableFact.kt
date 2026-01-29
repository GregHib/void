package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equips
import world.gregs.voidps.engine.inv.inventory

data class EquipsItem(
    val id: String,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = bot.player.equips(id, amount)
}

data class HasVariable(
    val id: String,
    val value: Any? = null,
) : Fact(1) {
    override fun check(bot: Bot) = bot.player.variables.get<Any>(id) == value
}

data class CarriesItem(
    val id: String,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = bot.player.carriesItem(id, amount)
}

data class EquipsOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.equips(id, amount) }
}

data class CarriesOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.carriesItem(id, amount) }
}

data class HasInventorySpace(
    val amount: Int,
) : Fact(10) {
    override fun check(bot: Bot) = bot.player.inventory.spaces >= amount
}

data class AtLocation(
    val id: String,
) : Fact(1000) {
    override fun check(bot: Bot) = bot.player.tile in Areas[id]
}

data class AtTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : Fact(1100) {
    override fun check(bot: Bot) = bot.player.tile.within(x, y, radius, level)
}