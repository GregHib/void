package world.gregs.voidps.world.interact.entity.player.display

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.contain.sendContainer
import world.gregs.voidps.engine.data.definition.extra.EnumDefinitions
import world.gregs.voidps.engine.data.definition.extra.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.armParam
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.onStyle
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.wristParam

val enums: EnumDefinitions by inject()
val structs: StructDefinitions by inject()

on<InterfaceOpened>({ id == "character_creation" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "skin_colour", 0 until enums.get("character_skin_interfaces").length)
    player.interfaceOptions.unlockAll(id, "colours", 0 until enums.get("character_top_interfaces").length)
    player.interfaceOptions.unlockAll(id, "styles", 0 until enums.get("character_top_styles_female").length)
    player.setVar("character_creation_female", !player.body.male)
    player.sendVariable("character_creation_style")
    player.sendVariable("character_creation_sub_style")
    player.sendVariable("character_creation_hair_style")
    player.sendVariable("character_creation_colour_offset")
    for (i in 1 until 20) {
        player.sendContainer("character_creation_${i}")
    }
}

on<InterfaceOption>({ id == "character_creation" && component == "female" }) { player: Player ->
    swapSex(player, true)
}

on<InterfaceOption>({ id == "character_creation" && component == "male" }) { player: Player ->
    swapSex(player, false)
}

on<InterfaceOption>({ id == "character_creation" && component == "skin_colour" }) { player: Player ->
    player.setVar("makeover_colour_skin", enums.get("character_skin").getInt(itemSlot))
}

on<InterfaceOption>({ id == "character_creation" && component.startsWith("style_") }) { player: Player ->
    val index = component.removePrefix("style_").toInt()
    updateStyle(player, index, 0)
}

on<InterfaceOption>({ id == "character_creation" && component.startsWith("type_") }) { player: Player ->
    val index = component.removePrefix("type_").toInt()
    val style: Int = player.getVar("character_creation_style")
    updateStyle(player, style - 1, index)
}

fun updateStyle(
    player: Player,
    styleIndex: Int = (player.getVar<Int>("character_creation_style") - 1).coerceAtLeast(0),
    subIndex: Int = (player.getVar<Int>("character_creation_sub_style") - 1).coerceAtLeast(0)
) {
    player.setVar("character_creation_style", styleIndex + 1)
    player.setVar("character_creation_sub_style", subIndex + 1)
    val struct = getStyleStruct(player, styleIndex, subIndex)
    player.setVar("makeover_top", struct.getParam(1182))
    player.setVar("makeover_arms", struct.getParam(1183))
    player.setVar("makeover_wrists", struct.getParam(1184))
    player.setVar("makeover_legs", struct.getParam(1185))
    player.setVar("makeover_shoes", struct.getParam(1186))
    updateColours(player, styleIndex, subIndex)
}

fun updateColours(
    player: Player,
    styleIndex: Int = (player.getVar<Int>("character_creation_style") - 1).coerceAtLeast(0),
    subIndex: Int = (player.getVar<Int>("character_creation_sub_style") - 1).coerceAtLeast(0),
    hairStyle: Int = player.getVar("character_creation_hair_style")
) {
    val struct = getStyleStruct(player, styleIndex, subIndex)
    val colour = hairStyle.rem(8) * 3L
    player.setVar("makeover_colour_top", struct.getParam(1187 + colour))
    player.setVar("makeover_colour_legs", struct.getParam(1188 + colour))
    player.setVar("makeover_colour_shoes", struct.getParam(1189 + colour))
}

on<InterfaceOption>({ id == "character_creation" && component.startsWith("part_") }) { player: Player ->
    val part = component.removePrefix("part_")
    player.setVar("character_part", part)
}

on<InterfaceOption>({ id == "character_creation" && component == "colours" }) { player: Player ->
    var part = player.getVar("character_part", "skin")
    if (part == "beard") {
        part = "hair"
    }
    player.setVar("makeover_colour_${part}", enums.get("character_$part").getInt(itemSlot))
}

on<InterfaceOption>({ id == "character_creation" && component == "choose_colour" }) { player: Player ->
    val colourProfile = (player.getVar<Int>("character_creation_colour_offset") + 1).rem(8)
    player.setVar("character_creation_colour_offset", colourProfile)
    updateColours(player, hairStyle = player.getVar<Int>("character_creation_hair_style") + colourProfile)
}

on<InterfaceOption>({ id == "character_creation" && component == "styles" }) { player: Player ->
    val sex = if (player.getVar("makeover_female", false)) "female" else "male"
    val part = player.getVar("character_part", "skin")
    val value = if (part == "hair") {
        enums.getStruct("character_${part}_styles_$sex", itemSlot, "id")
    } else {
        enums.get("character_${part}_styles_$sex").getInt(itemSlot)
    }
    if (part == "top") {
        onStyle(value) {
            setStyle(player, it.id)
            player.setVar("makeover_arms", it.getParam<Int>(armParam))
            player.setVar("makeover_wrists", it.getParam<Int>(wristParam))
        }
        player.setVar("character_creation_sub_style", 1)
    }
    player.setVar("character_creation_colour_offset", 0)
    player.setVar("makeover_${part}", value)
}

on<InterfaceOpened>({ id == "character_creation" }) { player: Player ->
    player.setVar("makeover_female", !player.body.male)
    player.setVar("makeover_hair", player.body.getLook(BodyPart.Hair))
    player.setVar("makeover_beard", player.body.getLook(BodyPart.Beard))
    player.setVar("makeover_top", player.body.getLook(BodyPart.Chest))
    player.setVar("makeover_arms", player.body.getLook(BodyPart.Arms))
    player.setVar("makeover_wrists", player.body.getLook(BodyPart.Hands))
    player.setVar("makeover_legs", player.body.getLook(BodyPart.Legs))
    player.setVar("makeover_shoes", player.body.getLook(BodyPart.Feet))
    player.setVar("makeover_colour_hair", player.body.getColour(BodyColour.Hair))
    player.setVar("makeover_colour_top", player.body.getColour(BodyColour.Top))
    player.setVar("makeover_colour_legs", player.body.getColour(BodyColour.Legs))
    player.setVar("makeover_colour_shoes", player.body.getColour(BodyColour.Feet))
    player.setVar("makeover_colour_skin", player.body.getColour(BodyColour.Skin))
}

on<InterfaceOption>({ id == "character_creation" && component == "confirm" }) { player: Player ->
    val male = !player.getVar<Boolean>("makeover_female")
    player.body.setLook(BodyPart.Hair, player.getVar("makeover_hair"))
    player.body.setLook(BodyPart.Beard, if (male) player.getVar("makeover_beard") else -1)
    player.body.male = male
    player.body.setLook(BodyPart.Chest, player.getVar("makeover_top"))
    player.body.setLook(BodyPart.Arms, player.getVar("makeover_arms"))
    player.body.setLook(BodyPart.Hands, player.getVar("makeover_wrists"))
    player.body.setLook(BodyPart.Legs, player.getVar("makeover_legs"))
    player.body.setLook(BodyPart.Feet, player.getVar("makeover_shoes"))
    player.body.setColour(BodyColour.Hair, player.getVar("makeover_colour_hair"))
    player.body.setColour(BodyColour.Top, player.getVar("makeover_colour_top"))
    player.body.setColour(BodyColour.Legs, player.getVar("makeover_colour_legs"))
    player.body.setColour(BodyColour.Feet, player.getVar("makeover_colour_shoes"))
    player.body.setColour(BodyColour.Skin, player.getVar("makeover_colour_skin"))
    player.flagAppearance()
    player.open(player.gameFrame.name)
}

fun setStyle(player: Player, id: Int) {
    val size = enums.get("character_styles").length
    val sex = if (player.getVar("makeover_female", false)) "female" else "male"
    for (index in 0 until size) {
        for (subIndex in 0..5) {
            val value = enums.getStruct("character_styles", index, "sub_style_${sex}_$subIndex", -1)
            if (value == id) {
                player.setVar("character_creation_style", index + 1)
                player.setVar("character_creation_sub_style", subIndex + 1)
                return
            }
        }
    }
}

fun swapSex(player: Player, female: Boolean) {
    player.setVar("makeover_female", female)
    player.setVar("character_creation_female", female)
    val hairStyle = player.getVar<Int>("character_creation_hair_style")
    val hair: Int = enums.getStruct("character_hair_styles_${if (female) "female" else "male"}", hairStyle, "id")
    val beard: Int = if (female) -1 else enums.get("character_beard_styles_male").getInt(hairStyle / 2)
    player.setVar("makeover_hair", hair)
    player.setVar("makeover_beard", beard)
    player.setVar("character_creation_sub_style", 1)
    updateStyle(player)
}

fun getStyleStruct(player: Player, styleIndex: Int, subIndex: Int): StructDefinition {
    val female = player.getVar("makeover_female", false)
    val sex = if (female) "female" else "male"
    val value: Int = enums.getStruct("character_styles", styleIndex, "sub_style_${sex}_${subIndex}")
    return structs.get(value)
}