package content.entity.npc.shop.stock

import content.entity.npc.shop.shopInventory
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged

/**
 * The item information side panel which shows a shop items requirements, stats and price
 */
val enums: EnumDefinitions by inject()

interfaceOption("Info", id = "shop") {
    val sample = component == "sample"
    val actualIndex = itemSlot / (if (sample) 4 else 6)
    val inventory = player.shopInventory(sample)
    val item = inventory[actualIndex]
    player["info_sample"] = sample
    player["info_index"] = actualIndex
    showInfo(player, item, actualIndex, sample)
}

interfaceOption("Close", "exit", "item_info") {
    player.open("shop_side")
    player.interfaceOptions.send("shop_side", "inventory")
}

inventoryChanged { player ->
    if (!player.contains("info_sample")) {
        return@inventoryChanged
    }
    val shop: String = player["shop"] ?: return@inventoryChanged
    val index: Int = player["info_index"] ?: return@inventoryChanged
    if (inventory == shop && this.index == index) {
        player["item_info_price"] = if (this.item.amount == 0) 0 else Price.getPrice(player, item.id, index, this.item.amount)
    }
}

fun showInfo(player: Player, item: Item, index: Int, sample: Boolean) {
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
        player["item_info_price"] = if (sample) {
            -1
        } else if (item.amount < 1) {
            item.amount
        } else {
            Price.getPrice(player, item.id, index, item.amount)
        }
        player["item_info_examine"] = "'${def["examine", "It's a null."]}'"
    }
}

fun setRequirements(player: Player, def: ItemDefinition) {
    val quest = def["quest_info", -1]
    if (def.contains("equip_req") || def.contains("skillcape_skill") || quest != -1) {
        player["item_info_requirement_title"] = enums.get("item_info_requirement_titles").getString(def.slot.index)
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
            val name: String = enums.getStruct("item_info_quests", quest, "interface_text")
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

fun defenceStatsColumn(def: ItemDefinition): String = """
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

val middleColumn = """
        
        Stab
        Slash
        Crush
        Magic
        Range
        Summoning
        <br><br><br><br><br><br>
""".trimIndent().replace("\n", "<br>")
