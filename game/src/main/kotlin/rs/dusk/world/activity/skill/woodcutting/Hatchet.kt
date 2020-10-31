package rs.dusk.world.activity.skill.woodcutting

import rs.dusk.engine.entity.character.contain.contains
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.skill.Level.has
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.utility.get

@Suppress("EnumEntryName")
enum class Hatchet(val index: Int) {
    // Regular hatchet indices taken from RS3 "Skilling Chances" spreadsheet
    Bronze_Hatchet(0),
    Iron_Hatchet(1),
    Dwarven_Army_Axe(1),
    Steel_Hatchet(2),
    Black_Hatchet(3),
    Mithril_Hatchet(4),
    Adamant_Hatchet(5),
    Rune_Hatchet(6),
    Dragon_Hatchet(7),
    Sacred_Clay_Hatchet(6),
    Volatile_Clay_Hatchet(6),
    Inferno_Adze(7),
    // Dungeoneering hatchet indices made up
    Novite_Hatchet(0),
    Bathus_Hatchet(1),
    Marmaros_Hatchet(2),
    Kratonite_Hatchet(3),
    Fractite_Hatchet(4),
    Zephyrium_Hatchet(5),
    Argonite_Hatchet(6),
    Katagon_Hatchet(7),
    Gorgonite_Hatchet(8),
    Promethium_Hatchet(9),
    Primal_Hatchet(10);

    val id: String = name.toLowerCase()

    fun calculateChance(treeHatchetDifferences: IntRange): Int {
        return (0 until index).sumBy { calculateHatchetChance(it, treeHatchetDifferences) }
    }

    val requiredLevel: Int
        get() = when (this) {
            Inferno_Adze -> 61
            Sacred_Clay_Hatchet, Volatile_Clay_Hatchet -> 50
            else -> get<ItemDefinitions>().get(id).getParam(750L, 0)
        }

    companion object {

        fun hasRequirements(player: Player, hatchet: Hatchet?, message: Boolean): Boolean {
            if (hatchet == null) {
                if (message) {
                    player.message("You need a hatchet to chop down this tree.")
                    player.message("You do not have a hatchet which you have the woodcutting level to use.")
                }
                return false
            }
            if (hatchet == Inferno_Adze && !player.has(Skill.Firemaking, 92, message)) {
                return false
            }
            if (!player.has(Skill.Woodcutting, hatchet.requiredLevel, message)) {
                return false
            }
            return true
        }

        fun get(player: Player): Hatchet? {
            val list = values().filter { hatchet -> hasRequirements(player, hatchet, false) && player.has(hatchet) }
            return list.maxBy { hatchet -> hatchet.requiredLevel }
        }

        private fun Player.has(hatchet: Hatchet) = inventory.contains(hatchet.id) || equipment.contains(hatchet.id)


        /**
         * Calculates the chance of success out of 256 given a [hatchet] and the hatchet chances for that tree [treeHatchetDifferences]
         * @param hatchet The index of the hatchet (0..7)
         * @param treeHatchetDifferences The min and max increase chance between each hatchet
         * @return chance of success
         */
        private fun calculateHatchetChance(hatchet: Int, treeHatchetDifferences: IntRange) = if (hatchet % 4 < 2) treeHatchetDifferences.last else treeHatchetDifferences.first

    }
}