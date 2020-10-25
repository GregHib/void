package rs.dusk.world.activity.skill.woodcutting

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

    companion object {


        /**
         * Calculates the chance of success out of 256 given a [hatchet] and the hatchet chances for that tree [treeHatchetDifferences]
         * @param hatchet The index of the hatchet (0..7)
         * @param treeHatchetDifferences The min and max increase chance between each hatchet
         * @return chance of success
         */
        private fun calculateHatchetChance(hatchet: Int, treeHatchetDifferences: IntRange) = if (hatchet % 4 < 2) treeHatchetDifferences.last else treeHatchetDifferences.first

    }
}