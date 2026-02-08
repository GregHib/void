package content.bot.req.fact

import content.bot.req.predicate.Predicate
import content.entity.player.bank.bank
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Tile

/**
 * TODO what is the purpose of a fact?
 *  1. provides current state of a player to Requirement/predicate
 *  2. Allows matching resolvers by specific produces keys
 *  3. Allows grouping requirements to listen for updates
 *  4. Determines order of requirements to be checked in
 *
 *
 * TODO you want to define configuration coarse level
 *      but evaluates at a fine level
 *      execution is at a course level
 *
 *      Coarse desired state expands into fine facts
 *
 * A bots state which can be a [content.bot.req.Requirement] for, or a product of performing a [content.bot.behaviour.Behaviour]
 * @param priority Ensure bots aren't walking to locations before getting items etc... lower values are prioritised first.
 */
sealed class Fact<T>(val priority: Int) {
    abstract fun getValue(player: Player): T

    /**
     * Fact specific identifiers for finding resolvers e.g. inv:bronze_hatchet, bank:coins etc...
     */
    open fun keys(): Set<String> = emptySet()

    /**
     * Group types to listen for types of updates e.g. inv:bank, enter:area etc...
     */
    open fun groups(): Set<String> = keys()

    object InventorySpace : Fact<Int>(10) {
        override fun keys() = setOf("inventory_space")
        override fun getValue(player: Player) = player.inventory.spaces
    }

    object InventoryItems : Fact<ItemView>(100) {
        override fun keys() = setOf("inv:inventory")
        override fun getValue(player: Player) = ItemView(player.inventory)
    }

    object EquipmentItems : Fact<ItemView>(90) {
        override fun keys() = setOf("inv:worn_equipment")
        override fun getValue(player: Player) = ItemView(player.equipment)
    }

    object BankItems : Fact<ItemView>(100) {
        override fun keys() = setOf("inv:bank")
        override fun getValue(player: Player) = ItemView(player.bank)
    }

    object AllItems : Fact<ItemView>(100) {
        override fun keys() = setOf("inv:inventory", "inv:bank", "inv:worn_equipment")
        override fun getValue(player: Player) = ItemView(player.inventory, player.bank, player.equipment)
    }

    data class IntVariable(val id: String, val default: Int) : Fact<Int>(1) {
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

    data class ClockRemaining(val clock: String, val seconds: Boolean = false) : Fact<Int>(1) {
        override fun keys() = setOf("var:$clock")
        override fun getValue(player: Player) = player.remaining(clock, if (seconds) epochSeconds() else GameLoop.tick)
    }

    data class HasTimer(val timer: String) : Fact<Boolean>(1) {
        override fun keys() = setOf("timer:$timer")
        override fun getValue(player: Player) = player.timers.contains(timer)
    }

    data class HasQueue(val queue: String) : Fact<Boolean>(1) {
        override fun keys() = setOf("queue:$queue")
        override fun getValue(player: Player) = player.queue.contains(queue)
    }

    data class InterfaceOpen(val id: String) : Fact<Boolean>(1) {
        override fun keys() = setOf("iface:$id")
        override fun getValue(player: Player) = player.interfaces.contains(id)
    }

    object PlayerTile : Fact<Tile>(1000) {
        override fun keys() = setOf("tile")
        override fun getValue(player: Player) = player.tile
    }

    object PlayerLevel : Fact<Int>(1000) {
        override fun keys() = setOf("level")
        override fun getValue(player: Player) = player.tile.level
    }

    object CombatLevel : Fact<Int>(1) {
        override fun keys() = setOf("combat")
        override fun getValue(player: Player) = player.combatLevel
    }

    data class ObjectExists(val filter: Predicate<String>, val tile: Tile) : Fact<Boolean>(1) {
        override fun keys() = setOf("object")
        override fun getValue(player: Player): Boolean {
            for (obj in GameObjects.at(tile)) {
                if (filter.test(player, obj.id)) {
                    return true
                }
            }
            return false
        }
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
        val skill: Skill?,
    ) : Fact<Int>(0) {
        override fun keys() = setOf("skill:${skill!!.name.lowercase()}")
        override fun getValue(player: Player) = player.levels.get(skill!!)

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
