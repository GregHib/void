package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.extra.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

val enums: EnumDefinitions by inject()

on<NPCOption>({ operate && npc.id == "hairdresser" && option == "Talk-to" }) { player: Player ->
    npc<Happy>("""
        Good afternoon ${if (player.male) "sir" else "madam"}. In need of a haircut${if (player.male) " or shave" else ""} are
        we?
    """)
    val choice = choice("""
        Yes, please.
        No, thank you.
    """)
    when (choice) {
        1 -> {
            player<Talk>("Yes, please.")
            npc<Happy>("""
                Please select the hairstyle you would like from this
                brochure. I'll even throw in a free recolour.
            """)
            startHairdressing()
        }
        2 -> {
            player<Talk>("No, thank you.")
            npc<Talk>("Very well. Come back if you change your mind.")
        }
    }
}

on<NPCOption>({ operate && npc.id == "hairdresser" && option == "Hair-cut" }) { player: Player ->
    startHairdressing()
}

suspend fun NPCOption.startHairdressing() {
    player.closeDialogue()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        npc<Afraid>("""
            I don't feel comfortable cutting hair when you are
            wielding something. Please remove what you are holding
            first.
        """)
        return
    }
    if (player.equipped(EquipSlot.Hat).isNotEmpty()) {
        npc<Upset>("I can't cut your hair with that on your head.")
        return
    }
    openDressingRoom("hairdressers_salon")
}

on<InterfaceOpened>({ id == "hairdressers_salon" }) { player: Player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    val styles = enums.get("style_hair_${player.sex}")
    val colours = enums.get("colour_hair")
    player.interfaceOptions.unlockAll(id, "styles", 0 until styles.length * 2)
    player.interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
    player["makeover_hair"] = player.body.getLook(BodyPart.Hair)
    player["makeover_beard"] = player.body.getLook(BodyPart.Beard)
    player["makeover_colour_hair"] = player.body.getColour(BodyColour.Hair)
}

on<InterfaceOption>({ id == "hairdressers_salon" && component.startsWith("style_") }) { player: Player ->
    player["makeover_facial_hair"] = component == "style_beard"
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "styles" }) { player: Player ->
    val beard = player["makeover_facial_hair", false]
    val type = if (beard) "beard" else "hair"
    val key = "look_${type}_${player.sex}"
    val value = if (beard) {
        enums.get(key).getInt(itemSlot / 2)
    } else {
        enums.getStruct(key, itemSlot / 2, "id")
    }
    player["makeover_$type"] = value
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "colours" }) { player: Player ->
    player["makeover_colour_hair"] = enums.get("colour_hair").getInt(itemSlot / 2)
}

on<InterfaceClosed>({ id == "hairdressers_salon" }) { player: Player ->
    player.softTimers.stop("dressing_room")
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Hair, player["makeover_hair"])
    player.body.setLook(BodyPart.Beard, player["makeover_beard"])
    player.body.setColour(BodyColour.Hair, player["makeover_colour_hair"])
    player.flagAppearance()
    player.closeMenu()
    npc<Cheerful>("hairdresser", if (player.male) {
        listOf("An excellent choice, sir.", "Mmm... very distinguished!")
    } else {
        listOf("A marvellous choice. You look splendid!", "It really suits you!")
    }.random())
}