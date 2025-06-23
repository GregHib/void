package content.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition

object EquipBonuses {
    val names = listOf(
        "Stab" to "stab_attack",
        "Slash" to "slash_attack",
        "Crush" to "crush_attack",
        "Magic" to "magic_attack",
        "Range" to "range_attack",
        "Stab" to "stab_defence",
        "Slash" to "slash_defence",
        "Crush" to "crush_defence",
        "Magic" to "magic_defence",
        "Ranged" to "range_defence",
        "Summoning" to "summoning_defence",
        "Absorb Melee" to "absorb_melee",
        "Absorb Magic" to "absorb_magic",
        "Absorb Ranged" to "absorb_range",
        "Strength" to "strength",
        "Ranged Strength" to "ranged_strength",
        "Prayer" to "prayer_bonus",
        "Magic Damage" to "magic_damage",
    )

    fun getValue(item: ItemDefinition, key: String): String? {
        val value = item[key, 0]
        if (value == 0) {
            return null
        }
        return format(key, value, false)
    }

    fun format(key: String, value: Int, bonuses: Boolean): String = when (key) {
        "magic_damage", "absorb_melee", "absorb_magic", "absorb_range" -> "${if (value >= 0) "+" else "-"}$value%"
        "strength", "ranged_strength" -> "${if (value > 0) {
            "+"
        } else if (value < 0) {
            "-"
        } else {
            ""
        }}${value.toDouble()}"
        else -> if (bonuses) "${if (value >= 0) "+" else "-"}$value" else value.toString()
    }
}
