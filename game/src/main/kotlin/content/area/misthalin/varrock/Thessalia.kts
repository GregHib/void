package content.area.misthalin.varrock

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop
import world.gregs.voidps.world.interact.entity.player.display.CharacterStyle.onStyle
import content.area.asgarnia.falador.openDressingRoom

val enums: EnumDefinitions by inject()

npcOperate("Talk-to", "thessalia") {
    npc<Happy>("Would you like to buy any fine clothes?")
    npc<Happy>("Or if you're more after fancy dress costumes or commemorative capes, talk to granny Iffie.")
    choice {
        option<Quiz>("What do you have?") {
            npc<Happy>("Well, I have a number of fine pieces of clothing on sale or, if you prefer, I can offer you an exclusive, total clothing makeover?")
            choice {
                option<Quiz>("Tell me more about this makeover.") {
                    npc<Happy>("Certainly!")
                    npc<Happy>("Here at Thessalia's Fine Clothing Boutique we offer a unique service, where we will totally revamp your outfit to your choosing. Tired of always wearing the same old outfit, day-in, day-out? Then this is the service for you!")
                    npc<Happy>("So, what do you say? Interested?")
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

npcOperate("Change-clothes", "thessalia") {
    startMakeover()
}

fun PlayerChoice.openShop(): Unit = option("I'd just like to buy some clothes.") {
    player.openShop("thessalias_fine_clothes")
}

suspend fun Interaction<Player>.startMakeover() {
    player.closeDialogue()
    if (!player.equipment.isEmpty()) {
        npc<Talk>("You're not able to try on my clothes with all that armour. Take it off and then speak to me again.")
        return
    }
    openDressingRoom("thessalias_makeovers")
}

interfaceOpen("thessalias_makeovers") { player ->
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

interfaceClose("thessalias_makeovers") { player ->
    player.softTimers.stop("dressing_room")
}

interfaceOption(component = "part_*", id = "thessalias_makeovers") {
    player["makeover_body_part"] = component.removePrefix("part_")
}

interfaceOption(component = "styles", id = "thessalias_makeovers") {
    val part = player["makeover_body_part", "top"]
    val previous = fullBodyChest(player["makeover_top", 0], player.male)
    if ((part == "arms" || part == "wrists") && previous) {
        return@interfaceOption
    }
    val value = enums.get("look_${part}_${player.sex}").getInt(itemSlot / 2)
    if (part == "top") {
        val current = fullBodyChest(value, player.male)
        if (previous && !current) {
            setDefaultArms(player)
        } else if (current) {
            onStyle(value) {
                player["makeover_arms"] = it.get<Int>("character_style_arms")
                player["makeover_wrists"] = it.get<Int>("character_style_wrists")
            }
        }
    }
    player["makeover_${part}"] = value
}

interfaceOption(component = "colours", id = "thessalias_makeovers") {
    val part = player["makeover_body_part", "top"]
    val colour = when (part) {
        "top", "arms" -> "makeover_colour_top"
        "legs" -> "makeover_colour_legs"
        else -> return@interfaceOption
    }
    player[colour] = enums.get("colour_$part").getInt(itemSlot / 2)
}

interfaceOption(component = "confirm", id = "thessalias_makeovers") {
    player.body.setLook(BodyPart.Chest, player["makeover_top", 0])
    player.body.setLook(BodyPart.Arms, player["makeover_arms", 0])
    player.body.setLook(BodyPart.Hands, player["makeover_wrists", 0])
    player.body.setLook(BodyPart.Legs, player["makeover_legs", 0])
    player.body.setColour(BodyColour.Top, player["makeover_colour_top", 0])
    player.body.setColour(BodyColour.Legs, player["makeover_colour_legs", 0])
    player.flagAppearance()
    player.closeMenu()
    npc<Happy>("thessalia", "A marvellous choice. You look splendid!")
}

fun fullBodyChest(look: Int, male: Boolean) = look in if (male) 443..474 else 556..587

fun setDefaultArms(player: Player) {
    val default = if (player.male) BodyParts.DEFAULT_LOOK_MALE else BodyParts.DEFAULT_LOOK_FEMALE
    player["makeover_arms"] = default[BodyPart.Arms.index]
    player["makeover_wrists"] = default[BodyPart.Hands.index]
}