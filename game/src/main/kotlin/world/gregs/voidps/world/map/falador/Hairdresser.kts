package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.definition.extra.EnumDefinitions
import world.gregs.voidps.engine.entity.character.clearGraphic
import world.gregs.voidps.engine.entity.character.mode.interact.clear
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

on<NPCOption>({ npc.id == "hairdresser" && option == "Talk-to" }) { player: Player ->
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
            player("talk", "Yes, please.")
            npc<Happy>("""
                Please select the hairstyle you would like from this
                brochure. I'll even throw in a free recolour.
            """)
            startHairdressing()
        }
        2 -> {
            player("talk", "No, thank you.")
            npc<Talk>("Very well. Come back if you change your mind.")
        }
    }
}

on<NPCOption>({ npc.id == "hairdresser" && option == "Hair-cut" }) { player: Player ->
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
    player.setVar("makeover_hair", player.body.getLook(BodyPart.Hair))
    player.setVar("makeover_beard", player.body.getLook(BodyPart.Beard))
    player.setVar("makeover_colour_hair", player.body.getColour(BodyColour.Hair))
}

on<InterfaceOption>({ id == "hairdressers_salon" && component.startsWith("style_") }) { player: Player ->
    player.setVar("makeover_facial_hair", component == "style_beard")
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "styles" }) { player: Player ->
    val beard = player.getVar("makeover_facial_hair", false)
    val type = if (beard) "beard" else "hair"
    val key = "look_${type}_${player.sex}"
    val value = if (beard) {
        enums.get(key).getInt(itemSlot / 2)
    } else {
        enums.getStruct(key, itemSlot / 2, "id")
    }
    player.setVar("makeover_$type", value)
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "colours" }) { player: Player ->
    player.setVar("makeover_colour_hair", enums.get("colour_hair").getInt(itemSlot / 2))
}

on<InterfaceClosed>({ id == "hairdressers_salon" }) { player: Player ->
    player.clear()
    player.clearGraphic()
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Hair, player.getVar("makeover_hair"))
    player.body.setLook(BodyPart.Beard, player.getVar("makeover_beard"))
    player.body.setColour(BodyColour.Hair, player.getVar("makeover_colour_hair"))
    player.flagAppearance()
    player.closeInterface()
    npc<Cheerful>("hairdresser", if (player.male) {
        listOf("An excellent choice, sir.", "Mmm... very distinguished!")
    } else {
        listOf("A marvellous choice. You look splendid!", "It really suits you!")
    }.random())
}