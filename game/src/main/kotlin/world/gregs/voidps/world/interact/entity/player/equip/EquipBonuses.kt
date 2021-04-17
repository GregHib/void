package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition

object EquipBonuses {

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
            "str", "range_str" -> "${if (value > 0) "+" else if (value < 0) "-" else ""}${value / 10.0}"
            else -> if(bonuses) "${if (value >= 0) "+" else "-"}${value}" else value.toString()
        }
    }
}