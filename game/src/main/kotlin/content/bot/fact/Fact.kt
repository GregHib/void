package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

/**
 * A bots state which can be a [Condition] for, or a product of performing a [content.bot.action.Behaviour]
 * @param priority Ensure bots aren't walking to locations before getting items etc... lower values are prioritised first.
 */
sealed class Fact<T>(val priority: Int) {
    abstract fun getValue(bot: Bot): T
    open fun keys(): Set<String> = emptySet()

    data class InventoryCount(val id: String) : Fact<Int>(100) {
        override fun keys() = setOf("inv:inventory")
        override fun getValue(bot: Bot) = bot.player.inventory.count(id)
    }

    object InventorySpace : Fact<Int>(10) {
        override fun keys() = setOf("inv:inventory")
        override fun getValue(bot: Bot) = bot.player.inventory.spaces
    }

    data class EquipCount(val id: String) : Fact<Int>(100) {
        override fun keys() = setOf("inv:equipment")
        override fun getValue(bot: Bot) = bot.player.equipment.count(id)
    }

    data class IntVariable(val id: String) : Fact<Int?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(bot: Bot) = bot.player.variables.get<Int>(id)
    }

    data class BoolVariable(val id: String) : Fact<Boolean?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(bot: Bot) = bot.player.variables.get<Boolean>(id)
    }

    data class StringVariable(val id: String) : Fact<String?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(bot: Bot) = bot.player.variables.get<String>(id)
    }

    data class DoubleVariable(val id: String) : Fact<Double?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(bot: Bot) = bot.player.variables.get<Double>(id)
    }

    object PlayerTile : Fact<Tile>(1000) {
        override fun keys() = setOf("tile")
        override fun getValue(bot: Bot) = bot.player.tile
    }

    object AttackLevel : SkillLevel(Skill.Attack)
    object DefenceLevel : SkillLevel(Skill.Defence)
    object StrengthLevel : SkillLevel(Skill.Strength)
    object ConstitutionLevel : SkillLevel(Skill.Constitution)
    object RangedLevel : SkillLevel(Skill.Ranged)
    object PrayerLevel : SkillLevel(Skill.Prayer)
    object MagicLevel : SkillLevel(Skill.Magic)
    object CookingLevel : SkillLevel(Skill.Cooking)
    object WoodcuttingLevel : SkillLevel(Skill.Woodcutting)
    object FletchingLevel : SkillLevel(Skill.Fletching)
    object FishingLevel : SkillLevel(Skill.Fishing)
    object FiremakingLevel : SkillLevel(Skill.Firemaking)
    object CraftingLevel : SkillLevel(Skill.Crafting)
    object SmithingLevel : SkillLevel(Skill.Smithing)
    object MiningLevel : SkillLevel(Skill.Mining)
    object HerbloreLevel : SkillLevel(Skill.Herblore)
    object AgilityLevel : SkillLevel(Skill.Agility)
    object ThievingLevel : SkillLevel(Skill.Thieving)
    object SlayerLevel : SkillLevel(Skill.Slayer)
    object FarmingLevel : SkillLevel(Skill.Farming)
    object RunecraftingLevel : SkillLevel(Skill.Runecrafting)
    object HunterLevel : SkillLevel(Skill.Hunter)
    object ConstructionLevel : SkillLevel(Skill.Construction)
    object SummoningLevel : SkillLevel(Skill.Summoning)
    object DungeoneeringLevel : SkillLevel(Skill.Dungeoneering)

    abstract class SkillLevel(
        val skill: Skill,
    ) : Fact<Int>(0) {
        override fun keys() = setOf("skill:${skill.name.lowercase()}")
        override fun getValue(bot: Bot) = bot.player.levels.get(skill)

        companion object {
            fun of(skill: String): SkillLevel = when (skill.lowercase()) {
                "attack" -> AttackLevel
                "defence" -> DefenceLevel
                "strength" -> StrengthLevel
                "constitution" -> ConstitutionLevel
                "ranged" -> RangedLevel
                "prayer" -> PrayerLevel
                "magic" -> MagicLevel
                "cooking" -> CookingLevel
                "woodcutting" -> WoodcuttingLevel
                "fletching" -> FletchingLevel
                "fishing" -> FishingLevel
                "firemaking" -> FiremakingLevel
                "crafting" -> CraftingLevel
                "smithing" -> SmithingLevel
                "mining" -> MiningLevel
                "herblore" -> HerbloreLevel
                "agility" -> AgilityLevel
                "thieving" -> ThievingLevel
                "slayer" -> SlayerLevel
                "farming" -> FarmingLevel
                "runecrafting" -> RunecraftingLevel
                "hunter" -> HunterLevel
                "construction" -> ConstructionLevel
                "summoning" -> SummoningLevel
                "dungeoneering" -> DungeoneeringLevel
                else -> throw IllegalArgumentException("Unknown skill: $skill")
            }
        }
    }

}
