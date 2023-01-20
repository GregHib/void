import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.delayForever
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

val enums: EnumDefinitions by inject()

on<NPCOption>({ npc.id == "hairdresser" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("happy", """
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
                npc("happy", """
                    Please select the hairstyle you would like from this
                    brochure. I'll even throw in a free recolour.
                """)
                startHairdressing(player, npc)
            }
            2 -> {
                player("talk", "No, thank you.")
                npc("talk", "Very well. Come back if you change your mind.")
            }
        }
    }
    delayForever()
}

on<NPCOption>({ npc.id == "hairdresser" && option == "Hair-cut" }) { player: Player ->
    startHairdressing(player, npc)
    delayForever()
}

fun startHairdressing(player: Player, npc: NPC) {
    player.dialogues.clear()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        player.talkWith(npc) {
            npc("afraid", """
                I don't feel comfortable cutting hair when you are
                wielding something. Please remove what you are holding
                first.
            """)
        }
        return
    }
    if (player.equipped(EquipSlot.Hat).isNotEmpty()) {
        player.talkWith(npc) {
            npc("upset", "I can't cut your hair with that on your head.")
        }
        return
    }
    player.action(ActionType.Makeover) {
        try {
            delay(1)
            player.setGraphic("dressing_room_start")
            delay(1)
            player.open("hairdressers_salon")
            while (isActive) {
                player.setGraphic("dressing_room")
                delay(1)
            }
        } finally {
            player.close("hairdressers_salon")
            player.setGraphic("dressing_room_finish")
            player.flagAppearance()
            withContext(NonCancellable) {
                delay(1)
            }
        }
    }
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
    player.action.cancel(ActionType.Makeover)
}

on<InterfaceOption>({ id == "hairdressers_salon" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Hair, player.getVar("makeover_hair"))
    player.body.setLook(BodyPart.Beard, player.getVar("makeover_beard"))
    player.body.setColour(BodyColour.Hair, player.getVar("makeover_colour_hair"))
    player.flagAppearance()
    player.closeInterface()
    player.dialogue {
        npc("hairdresser", "cheerful", if (player.male) {
            listOf("An excellent choice, sir.", "Mmm... very distinguished!")
        } else {
            listOf("A marvellous choice. You look splendid!", "It really suits you!")
        }.random())
    }
}