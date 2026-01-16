package content.area.misthalin.varrock

import content.area.asgarnia.falador.openDressingRoom
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.modal.CharacterStyle.onStyle
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart

class Thessalia(val enums: EnumDefinitions) : Script {

    init {
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

        interfaceOpened("thessalias_makeovers") { id ->
            interfaces.sendText(id, "confirm_text", "Change")
            interfaceOptions.unlockAll(id, "styles", 0 until 100)
            interfaceOptions.unlockAll(id, "colours", 0 until enums.get("colour_top").length * 2)
            set("makeover_top", body.getLook(BodyPart.Chest))
            set("makeover_arms", body.getLook(BodyPart.Arms))
            set("makeover_wrists", body.getLook(BodyPart.Hands))
            set("makeover_legs", body.getLook(BodyPart.Legs))
            set("makeover_colour_top", body.getColour(BodyColour.Top))
            set("makeover_colour_legs", body.getColour(BodyColour.Legs))
        }

        interfaceClosed("thessalias_makeovers") {
            softTimers.stop("dressing_room")
        }

        interfaceOption(id = "thessalias_makeovers:part_*") {
            set("makeover_body_part", it.component.removePrefix("part_"))
        }

        interfaceOption(id = "thessalias_makeovers:styles") { (_, itemSlot) ->
            val part = get("makeover_body_part", "top")
            val previous = fullBodyChest(get("makeover_top", 0), male)
            if ((part == "arms" || part == "wrists") && previous) {
                return@interfaceOption
            }
            val value = enums.get("look_${part}_$sex").getInt(itemSlot / 2)
            if (part == "top") {
                val current = fullBodyChest(value, male)
                if (previous && !current) {
                    setDefaultArms(this)
                } else if (current) {
                    onStyle(value) {
                        set("makeover_arms", it.get<Int>("character_style_arms"))
                        set("makeover_wrists", it.get<Int>("character_style_wrists"))
                    }
                }
            }
            set("makeover_$part", value)
        }

        interfaceOption(id = "thessalias_makeovers:colours") { (_, itemSlot) ->
            val part = get("makeover_body_part", "top")
            val colour = when (part) {
                "top", "arms" -> "makeover_colour_top"
                "legs" -> "makeover_colour_legs"
                else -> return@interfaceOption
            }
            set(colour, enums.get("colour_$part").getInt(itemSlot / 2))
        }

        interfaceOption("Confirm", "thessalias_makeovers:confirm") {
            body.setLook(BodyPart.Chest, get("makeover_top", 0))
            body.setLook(BodyPart.Arms, get("makeover_arms", 0))
            body.setLook(BodyPart.Hands, get("makeover_wrists", 0))
            body.setLook(BodyPart.Legs, get("makeover_legs", 0))
            body.setColour(BodyColour.Top, get("makeover_colour_top", 0))
            body.setColour(BodyColour.Legs, get("makeover_colour_legs", 0))
            flagAppearance()
            closeMenu()
            npc<Happy>("thessalia", "A marvellous choice. You look splendid!")
        }
    }

    fun ChoiceOption.openShop(): Unit = option("I'd just like to buy some clothes.") {
        openShop("thessalias_fine_clothes")
    }

    suspend fun Player.startMakeover() {
        closeDialogue()
        if (!equipment.isEmpty()) {
            npc<Neutral>("You're not able to try on my clothes with all that armour. Take it off and then speak to me again.")
            return
        }
        openDressingRoom("thessalias_makeovers")
    }

    fun fullBodyChest(look: Int, male: Boolean) = look in if (male) 443..474 else 556..587

    fun setDefaultArms(player: Player) {
        val default = if (player.male) BodyParts.DEFAULT_LOOK_MALE else BodyParts.DEFAULT_LOOK_FEMALE
        player["makeover_arms"] = default[BodyPart.Arms.index]
        player["makeover_wrists"] = default[BodyPart.Hands.index]
    }
}
