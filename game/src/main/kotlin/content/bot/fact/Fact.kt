package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equips
import world.gregs.voidps.engine.inv.inventory

/**
 * A bots state which can be required for, or a product of performing a [content.bot.action.Behaviour]
 * @param priority Ensure bots aren't walking to locations before getting items etc... lower values are prioritised first.
 */
sealed class Fact(val priority: Int) {
    open fun check(bot: Bot): Boolean = false
    open fun keys(): Set<String> = emptySet()
}

internal data class FactClone(
    val id: String,
) : Fact(-1)

internal data class FactReference(
    var fact: Fact,
    val references: Map<String, String>,
) : Fact(-1)

data class HasSkillLevel(
    val skill: Skill,
    val min: Int = 1,
    val max: Int = 120
) : Fact(0) {
    override fun check(bot: Bot) = bot.player.levels.get(skill) in min..max
    override fun keys() = setOf(skill.name)
}


data class EquipsItem(
    val id: String,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = bot.player.equips(id, amount)
    override fun keys() = setOf("inv:equipment")
}

data class HasVariable(
    val id: String,
    val value: Any? = null,
) : Fact(1) {
    override fun check(bot: Bot) = bot.player.variables.get<Any>(id) == value
    override fun keys() = setOf("var:${id}")
}

data class CarriesItem(
    val id: String,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = bot.player.carriesItem(id, amount)
    override fun keys() = setOf("inv:inventory")
}

data class EquipsOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.equips(id, amount) }
    override fun keys() = setOf("inv:equipment")
}

data class CarriesOne(
    val ids: Set<String>,
    val amount: Int = 1,
) : Fact(100) {
    override fun check(bot: Bot) = ids.any { id -> bot.player.carriesItem(id, amount) }
    override fun keys() = setOf("inv:inventory")
}

data class HasInventorySpace(
    val amount: Int,
) : Fact(10) {
    override fun check(bot: Bot) = bot.player.inventory.spaces >= amount
    override fun keys() = setOf("inv:inventory")
}

data class AtLocation(
    val id: String,
) : Fact(1000) {
    override fun check(bot: Bot) = bot.player.tile in Areas[id]
    override fun keys() = setOf("enter:$id")
}

data class AtTile(
    val x: Int,
    val y: Int,
    val level: Int,
    val radius: Int,
) : Fact(1100) {
    override fun check(bot: Bot) = bot.player.tile.within(x, y, radius, level)
    override fun keys() = setOf("tile")
}
