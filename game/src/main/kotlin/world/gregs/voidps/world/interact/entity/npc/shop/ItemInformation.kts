import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.StringVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.Price
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer
import world.gregs.voidps.world.interact.entity.player.equip.*

/**
 * The item information side panel which shows a shop items requirements, stats and price
 */
IntVariable(741, Variable.Type.VARC).register("info_item")
StringVariable(25, Variable.Type.VARCSTR).register("item_info_examine")
StringVariable(26, Variable.Type.VARCSTR).register("item_info_requirement")
StringVariable(34, Variable.Type.VARCSTR).register("item_info_requirement_title")
StringVariable(35, Variable.Type.VARCSTR).register("info_left")
StringVariable(36, Variable.Type.VARCSTR).register("info_middle")
StringVariable(52, Variable.Type.VARCSTR).register("info_right")
IntVariable(1242, Variable.Type.VARC).register("info_colour")
IntVariable(1241, Variable.Type.VARC).register("info_title_colour")
IntVariable(744, Variable.Type.VARC).register("item_info_price")

val enums: EnumDecoder by inject()
val structs: StructDecoder by inject()
val messages = enums.get(1434).map!!
val requirementMessages = enums.get(1435).map!!
val quests = enums.get(2252).map!!

on<InterfaceOption>({ name == "shop" && option == "Info" }) { player: Player ->
    val shop: String = player["shop"]
    val sample = component == "sample"
    val actualIndex = itemIndex / (if (sample) 4 else 6)
    val container = player.shopContainer(sample)
    val item = container.getItem(actualIndex)
    player["info_sample"] = sample
    showInfo(player, item, actualIndex, if (sample) "${shop}_sample" else shop, sample)
}

on<InterfaceOption>({ name == "item_info" && component == "exit" }) { player: Player ->
    player.open("shop_side")
    player.interfaceOptions.send("shop_side", "container")
}

on<InterfaceClosed>({ name == "item_info" }) { player: Player ->
    player.events.remove(player.getOrNull("item_info_bind"))
    player.clear("item_info_bind")
}

fun showInfo(player: Player, item: Item, index: Int, name: String, sample: Boolean) {
    player.open("item_info")
    if (item.isNotEmpty()) {
        player.setVar("info_title_colour", Colour.Orange.int)
        player.setVar("info_colour", Colour.Orange.int)
        player.setVar("info_item", item.id)
        val def = item.def
        if (def.options.contains("Wear") || def.options.contains("Wield")) {
            player.setVar("info_left", attackStatsColumn(def))
            player.setVar("info_middle", middleColumn)
            player.setVar("info_right", defenceStatsColumn(def))
            setRequirements(player, def)
        } else {
            player.setVar("info_left", "")
            player.setVar("info_middle", "")
            player.setVar("info_right", "")
            player.setVar("item_info_requirement_title", "")
        }
        player.setVar("item_info_price", if (sample) -1 else if (item.amount < 1) item.amount else Price.getPrice(player, item.id, index, item.amount))
        if (!sample) {
            val handler = player.events.on<Player, ItemChanged>({ container == name && this.index == index }) {
                player.setVar("item_info_price", if (this.item.amount == 0) 0 else Price.getPrice(player, item.id, index, this.item.amount))
            }
            player["item_info_bind"] = handler
        }
        player.setVar("item_info_examine", "'${def["examine", "It's a null."]}'")
    }
}

fun setRequirements(player: Player, def: ItemDefinition) {
    val slot = def["slot", EquipSlot.None]
    val quest = def.quest()
    if (def.hasRequirements() || quest != -1) {
        player.setVar("item_info_requirement_title", requirementMessages.getOrDefault(slot.index, ""))
        val builder = StringBuilder()
        for (i in 0 until 10) {
            val skill = def.requiredSkill(i) ?: break
            val level = def.requiredLevel(i)
            val colour = Colour.bool(player.has(skill, level, false))
            builder.append(colour.wrap("Level $level ${skill.name.toLowerCase()}<br>"))
        }
        val maxed = def.getMaxedSkill()
        if (maxed != null) {
            val colour = Colour.bool(player.has(maxed, maxed.maximum(), false))
            builder.append(colour.wrap("Level ${maxed.maximum()} ${maxed.name.toLowerCase()}<br>"))
        }
        if (quest != -1) {
            val structId = quests[quest] as Int
            val struct = structs.get(structId)
            val colour = Colour.bool(false)
            val name: String = struct.getParam(845)
            builder.append(colour.wrap("Quest complete: $name<br>"))
        }
        player.setVar("item_info_requirement", builder.toString())
    } else {
        player.setVar("item_info_requirement_title", messages.getOrDefault(slot.index, ""))
        player.setVar("item_info_requirement", "")
    }
}

fun getStat(definitions: ItemDefinition, key: String): String {
    val value = definitions[key, 0]
    return Colour.Yellow { if (value > 0) "+$value" else value.toString() }
}

fun attackStatsColumn(def: ItemDefinition): String = """
        Attack
        ${getStat(def, "stab")}
        ${getStat(def, "slash")}
        ${getStat(def, "crush")}
        ${getStat(def, "magic")}
        ${getStat(def, "range")}
        ${Colour.Yellow { "---" }}
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