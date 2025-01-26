package content.entity.player.modal

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import content.entity.player.modal.CharacterStyle.onStyle

val enums: EnumDefinitions by inject()
val structs: StructDefinitions by inject()

interfaceOpen("character_creation") { player ->
    player.interfaceOptions.unlockAll(id, "skin_colour", 0 until enums.get("character_skin_interfaces").length)
    player.interfaceOptions.unlockAll(id, "colours", 0 until enums.get("character_top_interfaces").length)
    player.interfaceOptions.unlockAll(id, "styles", 0 until enums.get("character_top_styles_female").length)
    player["character_creation_female"] = !player.body.male
    player.sendVariable("character_creation_style")
    player.sendVariable("character_creation_sub_style")
    player.sendVariable("character_creation_hair_style")
    player.sendVariable("character_creation_colour_offset")
    for (i in 1 until 20) {
        player.sendInventory("character_creation_${i}")
    }
}

interfaceClose("character_creation") { player ->
    for (i in 1 until 20) {
        player.inventories.clear("character_creation_${i}")
    }
}

interfaceOption(component = "female", id = "character_creation") {
    swapSex(player, true)
}

interfaceOption(component = "male", id = "character_creation") {
    swapSex(player, false)
}

interfaceOption(component = "skin_colour", id = "character_creation") {
    player["makeover_colour_skin"] = enums.get("character_skin").getInt(itemSlot)
}

interfaceOption(component = "style_*", id = "character_creation") {
    val index = component.removePrefix("style_").toInt()
    updateStyle(player, index, 0)
}

interfaceOption(component = "type_*", id = "character_creation") {
    val index = component.removePrefix("type_").toInt()
    val style: Int = player["character_creation_style", 0]
    updateStyle(player, style - 1, index)
}

fun updateStyle(
    player: Player,
    styleIndex: Int = (player["character_creation_style", 0] - 1).coerceAtLeast(0),
    subIndex: Int = (player["character_creation_sub_style", 0] - 1).coerceAtLeast(0)
) {
    player["character_creation_style"] = styleIndex + 1
    player["character_creation_sub_style"] = subIndex + 1
    val struct = getStyleStruct(player, styleIndex, subIndex)
    player["makeover_top"] = struct["character_style_top"]
    player["makeover_arms"] = struct["character_style_arms"]
    player["makeover_wrists"] = struct["character_style_wrists"]
    player["makeover_legs"] = struct["character_style_legs"]
    player["makeover_shoes"] = struct["character_style_shoes"]
    updateColours(player, styleIndex, subIndex)
}

fun updateColours(
    player: Player,
    styleIndex: Int = (player["character_creation_style", 0] - 1).coerceAtLeast(0),
    subIndex: Int = (player["character_creation_sub_style", 0] - 1).coerceAtLeast(0),
    hairStyle: Int = player["character_creation_hair_style", 0]
) {
    val struct = getStyleStruct(player, styleIndex, subIndex)
    val colour = hairStyle.rem(8)
    player["makeover_colour_top"] = struct["character_style_colour_top_$colour"]
    player["makeover_colour_legs"] = struct["character_style_colour_legs_$colour"]
    player["makeover_colour_shoes"] = struct["character_style_colour_shoes_$colour"]
}

interfaceOption(component = "part_*", id = "character_creation") {
    val part = component.removePrefix("part_")
    player["character_part"] = part
}

interfaceOption(component = "colours", id = "character_creation") {
    var part = player["character_part", "skin"]
    if (part == "beard") {
        part = "hair"
    }
    player["makeover_colour_${part}"] = enums.get("character_$part").getInt(itemSlot)
}

interfaceOption(component = "choose_colour", id = "character_creation") {
    val colourProfile = (player["character_creation_colour_offset", 0] + 1).rem(8)
    player["character_creation_colour_offset"] = colourProfile
    updateColours(player, hairStyle = player["character_creation_hair_style", 0] + colourProfile)
}

interfaceOption(component = "styles", id = "character_creation") {
    val sex = if (player["makeover_female", false]) "female" else "male"
    val part = player["character_part", "skin"]
    val value = if (part == "hair") {
        enums.getStruct("character_${part}_styles_$sex", itemSlot, "body_look_id")
    } else {
        enums.get("character_${part}_styles_$sex").getInt(itemSlot)
    }
    if (part == "top") {
        onStyle(value) {
            setStyle(player, it.id)
            player["makeover_arms"] = it.get<Int>("character_style_arms")
            player["makeover_wrists"] = it.get<Int>("character_style_wrists")
        }
        player["character_creation_sub_style"] = 1
    }
    player["character_creation_colour_offset"] = 0
    player["makeover_${part}"] = value
}

interfaceOpen("character_creation") { player ->
    player["makeover_female"] = !player.body.male
    player["makeover_hair"] = player.body.getLook(BodyPart.Hair)
    player["makeover_beard"] = player.body.getLook(BodyPart.Beard)
    player["makeover_top"] = player.body.getLook(BodyPart.Chest)
    player["makeover_arms"] = player.body.getLook(BodyPart.Arms)
    player["makeover_wrists"] = player.body.getLook(BodyPart.Hands)
    player["makeover_legs"] = player.body.getLook(BodyPart.Legs)
    player["makeover_shoes"] = player.body.getLook(BodyPart.Feet)
    player["makeover_colour_hair"] = player.body.getColour(BodyColour.Hair)
    player["makeover_colour_top"] = player.body.getColour(BodyColour.Top)
    player["makeover_colour_legs"] = player.body.getColour(BodyColour.Legs)
    player["makeover_colour_shoes"] = player.body.getColour(BodyColour.Feet)
    player["makeover_colour_skin"] = player.body.getColour(BodyColour.Skin)
}

interfaceOption(component = "confirm", id = "character_creation") {
    val male = !player["makeover_female", false]
    player.body.setLook(BodyPart.Hair, player["makeover_hair", 0])
    player.body.setLook(BodyPart.Beard, if (male) player["makeover_beard", 0] else -1)
    player.body.male = male
    player.body.setLook(BodyPart.Chest, player["makeover_top", 0])
    player.body.setLook(BodyPart.Arms, player["makeover_arms", 0])
    player.body.setLook(BodyPart.Hands, player["makeover_wrists", 0])
    player.body.setLook(BodyPart.Legs, player["makeover_legs", 0])
    player.body.setLook(BodyPart.Feet, player["makeover_shoes", 0])
    player.body.setColour(BodyColour.Hair, player["makeover_colour_hair", 0])
    player.body.setColour(BodyColour.Top, player["makeover_colour_top", 0])
    player.body.setColour(BodyColour.Legs, player["makeover_colour_legs", 0])
    player.body.setColour(BodyColour.Feet, player["makeover_colour_shoes", 0])
    player.body.setColour(BodyColour.Skin, player["makeover_colour_skin", 0])
    player.flagAppearance()
    player.open(player.interfaces.gameFrame)
}

fun setStyle(player: Player, id: Int) {
    val size = enums.get("character_styles").length
    val sex = if (player["makeover_female", false]) "female" else "male"
    for (index in 0 until size) {
        for (subIndex in 0..5) {
            val value = enums.getStruct("character_styles", index, "character_creation_sub_style_${sex}_$subIndex", -1)
            if (value == id) {
                player["character_creation_style"] = index + 1
                player["character_creation_sub_style"] = subIndex + 1
                return
            }
        }
    }
}

fun swapSex(player: Player, female: Boolean) {
    player["makeover_female"] = female
    player["character_creation_female"] = female
    val hairStyle = player["character_creation_hair_style", 0]
    val hair: Int = enums.getStruct("character_hair_styles_${if (female) "female" else "male"}", hairStyle, "body_look_id")
    val beard: Int = if (female) -1 else enums.get("character_beard_styles_male").getInt(hairStyle / 2)
    player["makeover_hair"] = hair
    player["makeover_beard"] = beard
    player["character_creation_sub_style"] = 1
    updateStyle(player)
}

fun getStyleStruct(player: Player, styleIndex: Int, subIndex: Int): StructDefinition {
    val female = player["makeover_female", false]
    val sex = if (female) "female" else "male"
    val value: Int = enums.getStruct("character_styles", styleIndex, "character_creation_sub_style_${sex}_${subIndex}")
    return structs.get(value)
}