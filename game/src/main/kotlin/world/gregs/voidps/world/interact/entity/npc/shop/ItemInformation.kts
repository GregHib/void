package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.*
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject

/**
 * The item information side panel which shows a shop items requirements, stats and price
 */
val enums: EnumDefinitions by inject()

on<InterfaceOption>({ id == "shop" && option == "Info" }) { player: Player ->
    val sample = component == "sample"
    val actualIndex = itemSlot / (if (sample) 4 else 6)
    val inventory = player.shopInventory(sample)
    val item = inventory[actualIndex]
    player["info_sample"] = sample
    player["info_index"] = actualIndex
    showInfo(player, item, actualIndex, sample)
}

on<InterfaceOption>({ id == "item_info" && component == "exit" }) { player: Player ->
    player.open("shop_side")
    player.interfaceOptions.send("shop_side", "inventory")
}

on<ItemChanged>({ it.contains("shop") && it.contains("info_sample") && it.contains("info_index") }) { player: Player ->
    val shop: String = player["shop"]
    val index: Int = player["info_index"]
    if (inventory == shop && this.index == index) {
        player["item_info_price"] = if (this.item.amount == 0) 0 else Price.getPrice(player, item.id, index, this.item.amount)
    }
}

fun showInfo(player: Player, item: Item, index: Int, sample: Boolean) {
    player.open("item_info")
    if (item.isNotEmpty()) {
        player["info_title_colour"] = Colours.orange
        player["info_colour"] = Colours.orange
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
        player["item_info_price"] = if (sample) -1 else if (item.amount < 1) item.amount else Price.getPrice(player, item.id, index, item.amount)
        player["item_info_examine"] = "'${def["examine", "It's a null."]}'"
    }
}

fun setRequirements(player: Player, def: ItemDefinition) {
    val quest = def.quest()
    if (def.hasRequirements() || quest != -1) {
        player["item_info_requirement_title"] = enums.get("item_info_requirement_titles").getString(def.slot.index)
        val builder = StringBuilder()
        for (i in 0 until 10) {
            val skill = def.requiredEquipSkill(i) ?: break
            val level = def.requiredEquipLevel(i)
            val colour = Colours.bool(player.has(skill, level, false))
            builder.append("<$colour>Level $level ${skill.name.lowercase()}<br>")
        }
        val maxed = def.getMaxedSkill()
        if (maxed != null) {
            val colour = Colours.bool(player.has(maxed, maxed.maximum(), false))
            builder.append("<$colour>Level ${maxed.maximum()} ${maxed.name.lowercase()}<br>")
        }
        if (quest != -1) {
            val colour = Colours.bool(false)
            val name: String = enums.getStruct("item_info_quests", quest, "quest_name")
            builder.append("<$colour>Quest complete: $name<br>")
        }
        player["item_info_requirement"] = builder.toString()
    } else {
        player["item_info_requirement_title"] = enums.get("item_info_titles").getString(def.slot.index)
        player["item_info_requirement"] = ""
    }
}

fun getStat(definitions: ItemDefinition, key: String): String {
    val value = definitions[key, 0]
    return "<yellow>${if (value > 0) "+$value" else value.toString()}"
}

fun attackStatsColumn(def: ItemDefinition): String = """
        Attack
        ${getStat(def, "stab")}
        ${getStat(def, "slash")}
        ${getStat(def, "crush")}
        ${getStat(def, "magic")}
        ${getStat(def, "range")}
        <yellow>---
        Strength
        Ranged Strength
        Magic Damage
        Absorb Melee
        Absorb Magic
        Absorb Ranged
        Prayer bonus
    """.trimIndent().replace("\n", "<br>")

fun defenceStatsColumn(def: ItemDefinition): String = """
        Defence
        ${getStat(def, "stab_def")}
        ${getStat(def, "slash_def")}
        ${getStat(def, "crush_def")}
        ${getStat(def, "magic_def")}
        ${getStat(def, "range_def")}
        ${getStat(def, "summoning_def")}
        ${getStat(def, "str")}
        ${getStat(def, "range_str")}
        ${getStat(def, "magic_damage")}
        ${getStat(def, "absorb_melee")}
        ${getStat(def, "absorb_magic")}
        ${getStat(def, "absorb_range")}
        ${getStat(def, "prayer")}
    """.trimIndent().replace("\n", "<br>")

val middleColumn = """
        
        Stab
        Slash
        Crush
        Magic
        Range
        Summoning
        <br><br><br><br><br><br>
    """.trimIndent().replace("\n", "<br>")