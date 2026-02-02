package content.bot.fact

import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

/**
 * A bots state which can be a [Condition] for, or a product of performing a [content.bot.action.Behaviour]
 * @param priority Ensure bots aren't walking to locations before getting items etc... lower values are prioritised first.
 */
sealed class Fact<T>(val priority: Int) {
    abstract fun getValue(player: Player): T
    open fun keys(): Set<String> = emptySet()

    object InventorySpace : Fact<Int>(10) {
        override fun keys() = setOf("inv:inventory")
        override fun getValue(player: Player) = player.inventory.spaces
    }

    data class InventoryCount(val id: String) : Fact<Int>(100) {
        override fun keys() = setOf("inv:inventory")
        override fun getValue(player: Player) = player.inventory.count(id)
    }

    data class ItemCount(val id: String) : Fact<Int>(100) {
        override fun keys() = setOf("inv:inventory", "inv:bank", "inv:equipment")
        override fun getValue(player: Player) = player.inventory.count(id) + player.bank.count(id) + player.equipment.count(id)
    }

    data class EquipCount(val id: String) : Fact<Int>(100) {
        override fun keys() = setOf("inv:equipment")
        override fun getValue(player: Player) = player.equipment.count(id)
    }

    data class IntVariable(val id: String, val default: Int?) : Fact<Int?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(player: Player) = player.variables.get(id) ?: default
    }

    data class BoolVariable(val id: String, val default: Boolean?) : Fact<Boolean?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(player: Player) = player.variables.get(id) ?: default
    }

    data class StringVariable(val id: String, val default: String?) : Fact<String?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(player: Player) = player.variables.get(id) ?: default
    }

    data class DoubleVariable(val id: String, val default: Double?) : Fact<Double?>(1) {
        override fun keys() = setOf("var:${id}")
        override fun getValue(player: Player) = player.variables.get(id) ?: default
    }

    object PlayerTile : Fact<Tile>(1000) {
        override fun keys() = setOf("tile")
        override fun getValue(player: Player) = player.tile
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
        override fun getValue(player: Player) = player.levels.get(skill)

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
