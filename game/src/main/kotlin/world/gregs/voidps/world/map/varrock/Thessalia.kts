package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.armParam
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.onStyle
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.wristParam
import world.gregs.voidps.world.map.falador.openDressingRoom

val enums: EnumDefinitions by inject()

on<NPCOption>({ operate && npc.id == "thessalia" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("Would you like to buy any fine clothes?")
    npc<Cheerful>("""
        Or if you're more after fancy dress costumes or
        commemorative capes, talk to granny Iffie.
    """)
    choice {
        option<Unsure>("What do you have?") {
            npc<Cheerful>("""
                Well, I have a number of fine pieces of clothing on sale or,
                if you prefer, I can offer you an exclusive, total clothing
                makeover?
            """)
            choice {
                option<Unsure>("Tell me more about this makeover.") {
                    npc<Cheerful>("Certainly!")
                    npc<Cheerful>("""
                        Here at Thessalia's Fine Clothing Boutique we offer a
                        unique service, where we will totally revamp your outfit to
                        your choosing. Tired of always wearing the same old
                        outfit, day-in, day-out? Then this is the service for you!
                    """)
                    npc<Cheerful>("So, what do you say? Interested?")
                    choice {
                        openShop()
                        option("No, thank you.")
                    }
                }
                openShop()
            }
        }
        option("No, thank you.")
    }
}

on<NPCOption>({ operate && npc.id == "thessalia" && option == "Change-clothes" }) { player: Player ->
    startMakeover()
}

fun PlayerChoice.changeOutfit(): Unit = option<Cheerful>("I'd like to change my outfit, please.") {
    if (!player.equipment.isEmpty()) {
        npc<Talk>("""
            You can't try them on while wearing armour. Take it off
            and speak to me again.
        """)
        return@option
    }
    npc<Cheerful>("""
        Wonderful. Feel free to try on some items and see if
        there's anything you would like.
    """)
    player<Cheerful>("Okay, thanks.")
    startMakeover()
}

fun PlayerChoice.openShop(): Unit = option("I'd just like to buy some clothes.") {
    player.openShop("thessalias_fine_clothes")
}

suspend fun PlayerContext.startMakeover() {
    player.closeDialogue()
    if (!player.equipment.isEmpty()) {
        npc<Talk>("""
            You're not able to try on my clothes with all that armour.
            Take it off and then speak to me again.
        """)
        return
    }
    openDressingRoom("thessalias_makeovers")
}

on<InterfaceOpened>({ id == "thessalias_makeovers" }) { player: Player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    player.interfaceOptions.unlockAll(id, "styles", 0 until 100)
    player.interfaceOptions.unlockAll(id, "colours", 0 until enums.get("colour_top").length * 2)
    player["makeover_top"] = player.body.getLook(BodyPart.Chest)
    player["makeover_arms"] = player.body.getLook(BodyPart.Arms)
    player["makeover_wrists"] = player.body.getLook(BodyPart.Hands)
    player["makeover_legs"] = player.body.getLook(BodyPart.Legs)
    player["makeover_colour_top"] = player.body.getColour(BodyColour.Top)
    player["makeover_colour_legs"] = player.body.getColour(BodyColour.Legs)
}

on<InterfaceClosed>({ id == "thessalias_makeovers" }) { player: Player ->
    player.softTimers.stop("dressing_room")
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component.startsWith("part_") }) { player: Player ->
    player["makeover_body_part"] = component.removePrefix("part_")
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "styles" }) { player: Player ->
    val part = player["makeover_body_part", "top"]
    val previous = fullBodyChest(player["makeover_top"], player.male)
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
                player["makeover_arms"] = it.getParam<Int>(armParam)
                player["makeover_wrists"] = it.getParam<Int>(wristParam)
            }
        }
    }
    player["makeover_${part}"] = value
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "colours" }) { player: Player ->
    val part = player["makeover_body_part", "top"]
    val colour = when (part) {
        "top", "arms" -> "makeover_colour_top"
        "legs" -> "makeover_colour_legs"
        else -> return@on
    }
    player[colour] = enums.get("colour_$part").getInt(itemSlot / 2)
}

on<InterfaceOption>({ id == "thessalias_makeovers" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Chest, player["makeover_top"])
    player.body.setLook(BodyPart.Arms, player["makeover_arms"])
    player.body.setLook(BodyPart.Hands, player["makeover_wrists"])
    player.body.setLook(BodyPart.Legs, player["makeover_legs"])
    player.body.setColour(BodyColour.Top, player["makeover_colour_top"])
    player.body.setColour(BodyColour.Legs, player["makeover_colour_legs"])
    player.flagAppearance()
    player.closeMenu()
    npc<Cheerful>("thessalia", "A marvellous choice. You look splendid!")
}

fun fullBodyChest(look: Int, male: Boolean) = look in if (male) 443..474 else 556..587

fun setDefaultArms(player: Player) {
    val default = if (player.male) BodyParts.DEFAULT_LOOK_MALE else BodyParts.DEFAULT_LOOK_FEMALE
    player["makeover_arms"] = default[BodyPart.Arms.index]
    player["makeover_wrists"] = default[BodyPart.Hands.index]
}