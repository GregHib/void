package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.equip.requiredLevel

enum class Hatchet(val index: Int) {
    // Regular hatchet indices taken from RS3 "Skilling Chances" spreadsheet
    BronzeHatchet(0),
    IronHatchet(1),
    SteelHatchet(2),
    BlackHatchet(3),
    MithrilHatchet(4),
    AdamantHatchet(5),
    RuneHatchet(6),
    DragonHatchet(7),
    SacredClayHatchet(6),
    VolatileClayHatchet(6),
    InfernoAdze(7),
    // Stealing creation
    HatchetClass1(0),
    HatchetClass2(1),
    HatchetClass3(2),
    HatchetClass4(3),
    HatchetClass5(4),
    // Dungeoneering hatchet indices made up
    NoviteHatchet(0),
    BathusHatchet(1),
    MarmarosHatchet(2),
    KratoniteHatchet(3),
    FractiteHatchet(4),
    ZephyriumHatchet(5),
    ArgoniteHatchet(6),
    KatagonHatchet(7),
    GorgoniteHatchet(8),
    PromethiumHatchet(9),
    PrimalHatchet(10);

    val id: String = name.toTitleCase().toUnderscoreCase()

    fun calculateChance(treeHatchetDifferences: IntRange): Int {
        return (0 until index).sumBy { calculateHatchetChance(it, treeHatchetDifferences) }
    }

    val requiredLevel: Int
        get() = when (this) {
            InfernoAdze -> 61
            SacredClayHatchet, VolatileClayHatchet -> 50
            else -> get<ItemDefinitions>().get(id).requiredLevel()
        }

    companion object {

        val regular = values().copyOfRange(0, 12)

        fun hasRequirements(player: Player, hatchet: Item?, message: Boolean = false): Boolean {
            return hasRequirements(player, get(hatchet?.name ?: return false) ?: return false, message)
        }

        fun hasRequirements(player: Player, hatchet: Hatchet?, message: Boolean = false): Boolean {
            if (hatchet == null) {
                if (message) {
                    player.message("You need a hatchet to chop down this tree.")
                    player.message("You do not have a hatchet which you have the woodcutting level to use.")
                }
                return false
            }
            if (hatchet == InfernoAdze && !player.has(Skill.Firemaking, 92, message)) {
                return false
            }
            if (!player.has(Skill.Woodcutting, hatchet.requiredLevel, message)) {
                return false
            }
            return true
        }

        fun get(player: Player): Hatchet? {
            val list = values().filter { hatchet -> hasRequirements(player, hatchet, false) && player.hasItem(hatchet.id) }
            return list.maxByOrNull { hatchet -> hatchet.index }
        }

        fun highest(player: Player): Hatchet? {
            return regular.lastOrNull { hatchet -> hasRequirements(player, hatchet, false) }
        }

        fun isHatchet(name: String): Boolean = name.endsWith("hatchet") || name == InfernoAdze.id

        fun isHatchet(item: Item): Boolean = isHatchet(item.name)

        fun get(name: String): Hatchet? {
            if (name.isBlank()) {
                return null
            }
            val name = name.toUnderscoreCase()
            for (hatchet in values()) {
                if (name == hatchet.id) {
                    return hatchet
                }
            }
            return null
        }

        /**
         * Calculates the chance of success out of 256 given a [hatchet] and the hatchet chances for that tree [treeHatchetDifferences]
         * @param hatchet The index of the hatchet (0..7)
         * @param treeHatchetDifferences The min and max increase chance between each hatchet
         * @return chance of success
         */
        private fun calculateHatchetChance(hatchet: Int, treeHatchetDifferences: IntRange) = if (hatchet % 4 < 2) treeHatchetDifferences.last else treeHatchetDifferences.first

    }
}