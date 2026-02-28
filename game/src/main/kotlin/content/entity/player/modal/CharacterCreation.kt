package content.entity.player.modal

import content.entity.player.modal.CharacterStyle.onStyle
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart

class CharacterCreation : Script {

    init {
        interfaceOpened("character_creation") { id ->
            interfaceOptions.unlockAll(id, "skin_colour", 0 until EnumDefinitions.get("character_skin_interfaces").length)
            interfaceOptions.unlockAll(id, "colours", 0 until EnumDefinitions.get("character_top_interfaces").length)
            interfaceOptions.unlockAll(id, "styles", 0 until EnumDefinitions.get("character_top_styles_female").length)
            set("character_creation_female", !body.male)
            sendVariable("character_creation_style")
            sendVariable("character_creation_sub_style")
            sendVariable("character_creation_hair_style")
            sendVariable("character_creation_colour_offset")
            for (i in 1 until 20) {
                sendInventory("character_creation_$i")
            }
        }

        interfaceClosed("character_creation") {
            for (i in 1 until 20) {
                inventories.clear("character_creation_$i")
            }
        }

        interfaceOption("Female", "character_creation:female") {
            swapSex(this, true)
        }

        interfaceOption("Male", "character_creation:male") {
            swapSex(this, false)
        }

        interfaceOption(id = "character_creation:skin_colour") { (_, itemSlot) ->
            set("makeover_colour_skin", EnumDefinitions.get("character_skin").int(itemSlot))
        }

        interfaceOption(id = "character_creation:style_*") {
            val index = it.component.removePrefix("style_").toInt()
            updateStyle(this, index, 0)
        }

        interfaceOption(id = "character_creation:type_*") {
            val index = it.component.removePrefix("type_").toInt()
            val style: Int = get("character_creation_style", 0)
            updateStyle(this, style - 1, index)
        }

        interfaceOption(id = "character_creation:part_*") {
            val part = it.component.removePrefix("part_")
            set("character_part", part)
        }

        interfaceOption(id = "character_creation:colours") { (_, itemSlot) ->
            var part = get("character_part", "skin")
            if (part == "beard") {
                part = "hair"
            }
            set("makeover_colour_$part", EnumDefinitions.get("character_$part").int(itemSlot))
        }

        interfaceOption("Choose My Colour", "character_creation:choose_colour") {
            val colourProfile = (get("character_creation_colour_offset", 0) + 1).rem(8)
            set("character_creation_colour_offset", colourProfile)
            updateColours(this, hairStyle = get("character_creation_hair_style", 0) + colourProfile)
        }

        interfaceOption(id = "character_creation:styles") { (_, itemSlot) ->
            val sex = if (get("makeover_female", false)) "female" else "male"
            val part = get("character_part", "skin")
            val value = if (part == "hair") {
                EnumDefinitions.getStruct("character_${part}_styles_$sex", itemSlot, "body_look_id")
            } else {
                EnumDefinitions.get("character_${part}_styles_$sex").int(itemSlot)
            }
            if (part == "top") {
                onStyle(value) {
                    setStyle(this, it.id)
                    set("makeover_arms", it.get<Int>("character_style_arms"))
                    set("makeover_wrists", it.get<Int>("character_style_wrists"))
                }
                set("character_creation_sub_style", 1)
            }
            set("character_creation_colour_offset", 0)
            set("makeover_$part", value)
        }

        interfaceOpened("character_creation") {
            set("makeover_female", !body.male)
            set("makeover_hair", body.getLook(BodyPart.Hair))
            set("makeover_beard", body.getLook(BodyPart.Beard))
            set("makeover_top", body.getLook(BodyPart.Chest))
            set("makeover_arms", body.getLook(BodyPart.Arms))
            set("makeover_wrists", body.getLook(BodyPart.Hands))
            set("makeover_legs", body.getLook(BodyPart.Legs))
            set("makeover_shoes", body.getLook(BodyPart.Feet))
            set("makeover_colour_hair", body.getColour(BodyColour.Hair))
            set("makeover_colour_top", body.getColour(BodyColour.Top))
            set("makeover_colour_legs", body.getColour(BodyColour.Legs))
            set("makeover_colour_shoes", body.getColour(BodyColour.Feet))
            set("makeover_colour_skin", body.getColour(BodyColour.Skin))
        }

        interfaceOption(id = "character_creation:confirm") {
            val male = !get("makeover_female", false)
            body.setLook(BodyPart.Hair, get("makeover_hair", 0))
            body.setLook(BodyPart.Beard, if (male) get("makeover_beard", 0) else -1)
            body.male = male
            body.setLook(BodyPart.Chest, get("makeover_top", 0))
            body.setLook(BodyPart.Arms, get("makeover_arms", 0))
            body.setLook(BodyPart.Hands, get("makeover_wrists", 0))
            body.setLook(BodyPart.Legs, get("makeover_legs", 0))
            body.setLook(BodyPart.Feet, get("makeover_shoes", 0))
            body.setColour(BodyColour.Hair, get("makeover_colour_hair", 0))
            body.setColour(BodyColour.Top, get("makeover_colour_top", 0))
            body.setColour(BodyColour.Legs, get("makeover_colour_legs", 0))
            body.setColour(BodyColour.Feet, get("makeover_colour_shoes", 0))
            body.setColour(BodyColour.Skin, get("makeover_colour_skin", 0))
            flagAppearance()
            open(interfaces.gameFrame)
        }
    }

    fun updateStyle(
        player: Player,
        styleIndex: Int = (player["character_creation_style", 0] - 1).coerceAtLeast(0),
        subIndex: Int = (player["character_creation_sub_style", 0] - 1).coerceAtLeast(0),
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
        hairStyle: Int = player["character_creation_hair_style", 0],
    ) {
        val struct = getStyleStruct(player, styleIndex, subIndex)
        val colour = hairStyle.rem(8)
        player["makeover_colour_top"] = struct["character_style_colour_top_$colour"]
        player["makeover_colour_legs"] = struct["character_style_colour_legs_$colour"]
        player["makeover_colour_shoes"] = struct["character_style_colour_shoes_$colour"]
    }

    fun setStyle(player: Player, id: Int) {
        val size = EnumDefinitions.get("character_styles").length
        val sex = if (player["makeover_female", false]) "female" else "male"
        for (index in 0 until size) {
            for (subIndex in 0..5) {
                val value = EnumDefinitions.getStruct("character_styles", index, "character_creation_sub_style_${sex}_$subIndex", -1)
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
        val hair: Int = EnumDefinitions.getStruct("character_hair_styles_${if (female) "female" else "male"}", hairStyle, "body_look_id")
        val beard: Int = if (female) -1 else EnumDefinitions.get("character_beard_styles_male").int(hairStyle / 2)
        player["makeover_hair"] = hair
        player["makeover_beard"] = beard
        player["character_creation_sub_style"] = 1
        updateStyle(player)
    }

    fun getStyleStruct(player: Player, styleIndex: Int, subIndex: Int): StructDefinition {
        val female = player["makeover_female", false]
        val sex = if (female) "female" else "male"
        val value: Int = EnumDefinitions.getStruct("character_styles", styleIndex, "character_creation_sub_style_${sex}_$subIndex")
        return StructDefinitions.get(value)
    }
}
