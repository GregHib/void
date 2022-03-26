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
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.definition.StructDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.armParam
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.onStyle
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.wristParam

val enums: EnumDefinitions by inject()
val structs: StructDefinitions by inject()

on<NPCOption>({ npc.id == "thessalia" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("cheerful", "Would you like to buy any fine clothes?")
        npc("cheerful", """
            Or if you're more after fancy dress costumes or
            commemorative capes, talk to granny Iffie.
        """)
        var choice = choice("""
            What do you have?
            No, thank you.
        """)
        if (choice == 1) {
            return@talkWith
        }
        player("unsure", "What do you have?")
        npc("cheerful", """
            Well, I have a number of fine pieces of clothing on sale or,
            if you prefer, I can offer you an exclusive, total clothing
            makeover?
        """)
        choice = choice("""
            Tell me more about this makeover.
            I'd just like to buy some clothes.
        """)
        if (choice == 2) {
            player.events.emit(OpenShop("thessalias_fine_clothes"))
            return@talkWith
        }
        player("unsure", "Tell me more about this makeover.")
        npc("cheerful", "Certainly!")
        npc("cheerful", """
            Here at Thessalia's Fine Clothing Boutique we offer a
            unique service, where we will totally revamp your outfit to
            your choosing. Tired of always wearing the same old
            outfit, day-in, day-out? Then this is the service for you!
        """)
        npc("cheerful", "So, what do you say? Interested?")
        choice = choice("""
            I'd like to change my outfit, please.
            I'd just like to buy some clothes.
            No, thank you.
        """)
        if (choice == 2) {
            player.events.emit(OpenShop("thessalias_fine_clothes"))
            return@talkWith
        }
        if (choice == 3) {
            return@talkWith
        }
        player("cheerful", "I'd like to change my outfit, please")
        if (!player.equipment.isEmpty()) {
            npc("talk", """
                You can't try them on while wearing armour. Take it off
                and speak to me again.
            """)
            return@talkWith
        }
        npc("cheerful", """
            Wonderful. Feel free to try on some items and see if
            there's anything you would like.
        """)
        player("cheerful", "Okay, thanks.")
        startMakeover(player, npc)
    }
}

on<NPCOption>({ npc.id == "thessalia" && option == "Change-clothes" }) { player: Player ->
    startMakeover(player, npc)
}

fun startMakeover(player: Player, npc: NPC) {
    player.dialogues.clear()
    if (!player.equipment.isEmpty()) {
        player.talkWith(npc) {
            npc("talk", """
                    You're not able to try on my clothes with all that armour.
                    Take it off and then speak to me again.
                """)
        }
        return
    }
    player.action(ActionType.Makeover) {
        try {
            delay(1)
            player.setGraphic("dressing_room_start")
            delay(1)
            player.open("thessalias_makeovers")
            while (isActive) {
                player.setGraphic("dressing_room")
                delay(1)
            }
        } finally {
            player.close("thessalias_makeovers")
            player.setGraphic("dressing_room_finish")
            player.flagAppearance()
            withContext(NonCancellable) {
                delay(1)
            }
        }
    }
}

on<InterfaceOpened>({ id == "thessalias_makeovers" }) { player: Player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    player.interfaceOptions.unlockAll(id, "styles", 0 until 100)
    player.interfaceOptions.unlockAll(id, "colours", 0 until enums.get("colour_top").length * 2)
    player.setVar("makeover_top", player.body.getLook(BodyPart.Chest))
    player.setVar("makeover_arms", player.body.getLook(BodyPart.Arms))
    player.setVar("makeover_wrists", player.body.getLook(BodyPart.Hands))
    player.setVar("makeover_legs", player.body.getLook(BodyPart.Legs))
    player.setVar("makeover_colour_top", player.body.getColour(BodyColour.Top))
    player.setVar("makeover_colour_legs", player.body.getColour(BodyColour.Legs))
}

on<InterfaceClosed>({ id == "thessalias_makeovers" }) { player: Player ->
    player.action.cancel(ActionType.Makeover)
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component.startsWith("part_") }) { player: Player ->
    player.setVar("makeover_body_part", component.removePrefix("part_"))
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "styles" }) { player: Player ->
    val part = player.getVar("makeover_body_part", "top")
    val previous = fullBodyChest(player.getVar("makeover_top"), player.male)
    if ((part == "arms" || part == "wrists") && previous) {
        return@on
    }
    val value = enums.get("look_${part}_${player.sex}").getInt(itemSlot / 2)
    if (part == "top") {
        val current = fullBodyChest(value, player.male)
        if (previous && !current) {
            setDefaultArms(player)
        } else if (current) {
            onStyle(value) {
                player.setVar("makeover_arms", it.getParam<Int>(armParam))
                player.setVar("makeover_wrists", it.getParam<Int>(wristParam))
            }
        }
    }
    player.setVar("makeover_${part}", value)
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "colours" }) { player: Player ->
    val part = player.getVar("makeover_body_part", "top")
    val colour = when (part) {
        "top", "arms" -> "makeover_colour_top"
        "legs" -> "makeover_colour_legs"
        else -> return@on
    }
    player.setVar(colour, enums.get("colour_$part").getInt(itemSlot / 2))
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Chest, player.getVar("makeover_top"))
    player.body.setLook(BodyPart.Arms, player.getVar("makeover_arms"))
    player.body.setLook(BodyPart.Hands, player.getVar("makeover_wrists"))
    player.body.setLook(BodyPart.Legs, player.getVar("makeover_legs"))
    player.body.setColour(BodyColour.Top, player.getVar("makeover_colour_top"))
    player.body.setColour(BodyColour.Legs, player.getVar("makeover_colour_legs"))
    player.flagAppearance()
    player.closeInterface()
    player.dialogue {
        npc("thessalia", "cheerful", "A marvellous choice. You look splendid!")
    }
}

fun fullBodyChest(look: Int, male: Boolean) = look in if (male) 443..474 else 556..587

fun setDefaultArms(player: Player) {
    val default = if (player.male) BodyParts.DEFAULT_LOOK_MALE else BodyParts.DEFAULT_LOOK_FEMALE
    player.setVar("makeover_arms", default[BodyPart.Arms.index])
    player.setVar("makeover_wrists", default[BodyPart.Hands.index])
}