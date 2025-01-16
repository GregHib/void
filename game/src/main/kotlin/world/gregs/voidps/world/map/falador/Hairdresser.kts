package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

val enums: EnumDefinitions by inject()

npcOperate("Talk-to", "hairdresser") {
    npc<Pleased>("Good afternoon ${if (player.male) "sir" else "madam"}. In need of a haircut${if (player.male) " or shave" else ""} are we?")
    choice {
        option<Talk>("Yes, please.") {
            npc<Pleased>("Please select the hairstyle you would like from this brochure. I'll even throw in a free recolour.")
            startHairdressing()
        }
        option<Talk>("No, thank you.") {
            npc<Talk>("Very well. Come back if you change your mind.")
        }
    }
}

npcOperate("Hair-cut", "hairdresser") {
    startHairdressing()
}

suspend fun NPCOption<Player>.startHairdressing() {
    player.closeDialogue()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        npc<Afraid>("I don't feel comfortable cutting hair when you are wielding something. Please remove what you are holding first.")
        return
    }
    if (player.equipped(EquipSlot.Hat).isNotEmpty()) {
        npc<Upset>("I can't cut your hair with that on your head.")
        return
    }
    openDressingRoom("hairdressers_salon")
}

interfaceOpen("hairdressers_salon") { player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    val styles = enums.get("style_hair_${player.sex}")
    val colours = enums.get("colour_hair")
    player.interfaceOptions.unlockAll(id, "styles", 0 until styles.length * 2)
    player.interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
    player["makeover_hair"] = player.body.getLook(BodyPart.Hair)
    player["makeover_beard"] = player.body.getLook(BodyPart.Beard)
    player["makeover_colour_hair"] = player.body.getColour(BodyColour.Hair)
}

interfaceOption(component = "style_*", id = "hairdressers_salon") {
    player["makeover_facial_hair"] = component == "style_beard"
}

interfaceOption(component = "styles", id = "hairdressers_salon") {
    val beard = player["makeover_facial_hair", false]
    val type = if (beard) "beard" else "hair"
    val key = "look_${type}_${player.sex}"
    val value = if (beard) {
        enums.get(key).getInt(itemSlot / 2)
    } else {
        enums.getStruct(key, itemSlot / 2, "body_look_id")
    }
    player["makeover_$type"] = value
}

interfaceOption(component = "colours", id = "hairdressers_salon") {
    player["makeover_colour_hair"] = enums.get("colour_hair").getInt(itemSlot / 2)
}

interfaceClose("hairdressers_salon") { player ->
    player.softTimers.stop("dressing_room")
}

interfaceOption(component = "confirm", id = "hairdressers_salon") {
    player.body.setLook(BodyPart.Hair, player["makeover_hair", 0])
    player.body.setLook(BodyPart.Beard, player["makeover_beard", 0])
    player.body.setColour(BodyColour.Hair, player["makeover_colour_hair", 0])
    player.flagAppearance()
    player.closeMenu()
    npc<Happy>("hairdresser", if (player.male) {
        listOf("An excellent choice, sir.", "Mmm... very distinguished!")
    } else {
        listOf("A marvellous choice. You look splendid!", "It really suits you!")
    }.random())
}