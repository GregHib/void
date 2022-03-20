import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.StructDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart

val enums: EnumDefinitions by inject()
val definitions: InterfaceDefinitions by inject()
val structs: StructDefinitions by inject()

on<InterfaceOpened>({ id == "character_creation" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "skin_colour", 0 until enums.get("character_skin_colours").length)
    player.interfaceOptions.unlockAll(id, "colours", 0 until enums.get("character_torso_colours").length)
    player.interfaceOptions.unlockAll(id, "styles", 0 until enums.get("character_torso_styles_female").length)
}

on<InterfaceOption>({ id == "character_creation" && component == "female" }) { player: Player ->
    player.setVar("makeover_female", true)
    swapSex(player, false)
}

on<InterfaceOption>({ id == "character_creation" && component == "male" }) { player: Player ->
    player.setVar("makeover_female", false)
    swapSex(player, true)
}

on<InterfaceOption>({ id == "character_creation" && component == "skin_colour" }) { player: Player ->
    player.setVar("makeover_colour_skin", enums.get("character_skin_colours").getInt(itemSlot))
}

on<InterfaceOption>({ id == "character_creation" && component.startsWith("part_") }) { player: Player ->
    player.setVar("character_part", component.removePrefix("part_"))
}

on<InterfaceOption>({ id == "character_creation" && component == "colours" }) { player: Player ->
    var part = player.getVar("character_part", "skin")
    if (part == "beard") {
        part = "hair"
    }
    player.setVar("makeover_colour_${part}", enums.get("character_$part").getInt(itemSlot))
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
        setFullBodyArms(value, player)
    }
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
//    player.body.setLook(BodyPart.Arms, player.getVar("makeover_arms"))
//    player.body.setLook(BodyPart.Hands, player.getVar("makeover_wrists"))
//    player.body.setLook(BodyPart.Legs, player.getVar("makeover_legs"))
//    player.body.setLook(BodyPart.Feet, player.getVar("makeover_shoes"))
//    player.body.setColour(BodyColour.Hair, player.getVar("makeover_colour_hair"))
//    player.body.setColour(BodyColour.Top, player.getVar("makeover_colour_top"))
//    player.body.setColour(BodyColour.Legs, player.getVar("makeover_colour_legs"))
//    player.body.setColour(BodyColour.Feet, player.getVar("makeover_colour_shoes"))
//    player.body.setColour(BodyColour.Skin, player.getVar("makeover_colour_skin"))
    player.flagAppearance()
    player.open(player.gameFrame.name)
}

fun swapSex(player: Player, male: Boolean) {
    swapLook(player, male, "arms")
    swapLook(player, male, "wrists")
    swapLook(player, male, "legs")
    swapLook(player, male, "top")
    swapLook(player, male, "shoes")
}

fun swapLook(player: Player, male: Boolean, name: String) {
    val old = enums.get("look_${name}_${if (male) "female" else "male"}")
    val new = enums.get("look_${name}_${if (male) "male" else "female"}")
    val key = old.getKey(player.getVar<Int>("makeover_$name"))
    println("Swap $name from ${player.getVar<Int>("makeover_$name")} to ${new.getInt(key)}")
    player.setVar("makeover_$name", new.getInt(key))
}

val styleCount = 64
val styleStruct = 1048
val topStyle = 1182L
val armStyle = 1183L
val wristStyle = 1184L

fun setFullBodyArms(value: Int, player: Player) {
    for (i in 0 until styleCount) {
        val style = structs.get(styleStruct + i)
        if (style.getParam<Int>(topStyle) == value) {
            player.setVar("makeover_arms", style.getParam<Int>(armStyle))
            player.setVar("makeover_wrists", style.getParam<Int>(wristStyle))
            break
        }
    }
}