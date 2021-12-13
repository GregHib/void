package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition

object EquipBonuses {
    val names = listOf(
        "Stab" to "stab",
        "Slash" to "slash",
        "Crush" to "crush",
        "Magic" to "magic",
        "Range" to "range",
        "Stab" to "stab_def",
        "Slash" to "slash_def",
        "Crush" to "crush_def",
        "Magic" to "magic_def",
        "Ranged" to "range_def",
        "Summoning" to "summoning_def",
        "Absorb Melee" to "absorb_melee",
        "Absorb Magic" to "absorb_magic",
        "Absorb Ranged" to "absorb_range",
        "Strength" to "str",
        "Ranged Strength" to "range_str",
        "Prayer" to "prayer",
        "Magic Damage" to "magic_damage"
    )
    val nameMap = names.toMap()

    fun getValue(item: ItemDefinition, key: String): String? {
        val value = item[key, 0]
        if (value == 0) {
            return null
        }
        return format(key, value, false)
    }

    fun format(key: String, value: Int, bonuses: Boolean): String {
        return when (key) {
            "magic_damage", "absorb_melee", "absorb_magic", "absorb_range" -> "${if (value >= 0) "+" else "-"}${value}%"
            "str", "range_str" -> "${if (value > 0) "+" else if (value < 0) "-" else ""}${value.toDouble()}"
            else -> if(bonuses) "${if (value >= 0) "+" else "-"}${value}" else value.toString()
        }
    }
}