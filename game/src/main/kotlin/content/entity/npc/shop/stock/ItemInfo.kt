package content.entity.npc.shop.stock

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot

object ItemInfo {

    fun showInfo(player: Player, item: Item, price: Int = -1) {
        player.open("item_info")
        if (item.isNotEmpty()) {
            player["info_title_colour"] = Colours.ORANGE
            player["info_colour"] = Colours.ORANGE
            player["info_item"] = item.def.id
            val def = item.def
            if (def.options.contains("Wear") || def.options.contains("Wield")) {
                player["info_left"] = attackStatsColumn(def)
                player["info_middle"] = middleColumn
                player["info_right"] = defenceStatsColumn(def)
                setRequirements(player, def)
            } else {
                player["info_left"] = ""
                player["info_middle"] = ""
                player["info_right"] = ""
                player["item_info_requirement_title"] = ""
            }
            player["item_info_price"] = price
            player["item_info_examine"] = "'${def["examine", "It's a null."]}'"
        }
    }

    private fun setRequirements(player: Player, def: ItemDefinition) {
        val quest = def["quest_info", -1]
        if (def.contains("equip_req") || def.contains("skillcape_skill") || quest != -1) {
            player["item_info_requirement_title"] = EnumDefinitions.get("item_info_requirement_titles").getString(def.slot.index)
            val builder = StringBuilder()
            val requirements = def.getOrNull<Map<Skill, Int>>("equip_req") ?: emptyMap()
            for ((skill, level) in requirements) {
                val colour = Colours.bool(player.has(skill, level, false))
                builder.append("<$colour>Level $level ${skill.name.lowercase()}<br>")
            }
            val maxed: Skill? = def.getOrNull("skillcape_skill")
            if (maxed != null) {
                val colour = Colours.bool(player.has(maxed, maxed.maximum(), false))
                builder.append("<$colour>Level ${maxed.maximum()} ${maxed.name.lowercase()}<br>")
            }
            if (quest != -1) {
                val colour = Colours.bool(false)
                val name: String = EnumDefinitions.getStruct("item_info_quests", quest, "interface_text")
                builder.append("<$colour>Quest complete: $name<br>")
            }
            player["item_info_requirement"] = builder.toString()
        } else {
            player["item_info_requirement_title"] = EnumDefinitions.get("item_info_titles").getString(def.slot.index)
            player["item_info_requirement"] = ""
        }
    }

    private fun getStat(definitions: ItemDefinition, key: String): String {
        val value = definitions[key, 0]
        return "<yellow>${if (value > 0) "+$value" else value.toString()}"
    }

    private fun attackStatsColumn(def: ItemDefinition): String = """
            Attack
            ${getStat(def, "stab_attack")}
            ${getStat(def, "slash_attack")}
            ${getStat(def, "crush_attack")}
            ${getStat(def, "magic_attack")}
            ${getStat(def, "range_attack")}
            <yellow>---
            Strength
            Ranged Strength
            Magic Damage
            Absorb Melee
            Absorb Magic
            Absorb Ranged
            Prayer bonus
    """.trimIndent().replace("\n", "<br>")

    private fun defenceStatsColumn(def: ItemDefinition): String = """
            Defence
            ${getStat(def, "stab_defence")}
            ${getStat(def, "slash_defence")}
            ${getStat(def, "crush_defence")}
            ${getStat(def, "magic_defence")}
            ${getStat(def, "range_defence")}
            ${getStat(def, "summoning_defence")}
            ${getStat(def, "strength")}
            ${getStat(def, "ranged_strength")}
            ${getStat(def, "magic_damage")}
            ${getStat(def, "absorb_melee")}
            ${getStat(def, "absorb_magic")}
            ${getStat(def, "absorb_range")}
            ${getStat(def, "prayer_bonus")}
    """.trimIndent().replace("\n", "<br>")

    private val middleColumn = """
            
            Stab
            Slash
            Crush
            Magic
            Range
            Summoning
            <br><br><br><br><br><br>
    """.trimIndent().replace("\n", "<br>")
}
