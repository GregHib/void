package rs.dusk.world.activity.skill.woodcutting

@Suppress("EnumEntryName")
enum class Hatchet {
    Bronze_Hatchet,
    Iron_Hatchet,
    Black_Hatchet,
    Dwarven_Army_Axe,
    Steel_Hatchet,
    Mithril_Hatchet,
    Adamant_Hatchet,
    Rune_Hatchet,
    Dragon_Hatchet,
    Sacred_Clay_Hatchet,
    Volatile_Clay_Hatchet,
    Inferno_Adze,
    Novite_Hatchet,
    Bathus_Hatchet,
    Marmaros_Hatchet,
    Kratonite_Hatchet,
    Fractite_Hatchet,
    Zephyrium_Hatchet,
    Argonite_Hatchet,
    Katagon_Hatchet,
    Gorgonite_Hatchet,
    Promethium_Hatchet,
    Primal_Hatchet;

    val id: String = name.toLowerCase()
}