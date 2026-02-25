package content.area.asgarnia.falador

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Hairdresser : Script {

    init {
        npcOperate("Talk-to", "hairdresser") {
            npc<Pleased>("Good afternoon ${if (male) "sir" else "madam"}. In need of a haircut${if (male) " or shave" else ""} are we?")
            choice {
                option<Neutral>("Yes, please.") {
                    npc<Pleased>("Please select the hairstyle you would like from this brochure. I'll even throw in a free recolour.")
                    startHairdressing()
                }
                option<Neutral>("No, thank you.") {
                    npc<Neutral>("Very well. Come back if you change your mind.")
                }
            }
        }

        npcOperate("Hair-cut", "hairdresser") {
            startHairdressing()
        }

        interfaceOpened("hairdressers_salon") { id ->
            interfaces.sendText(id, "confirm_text", "Change")
            val styles = EnumDefinitions.get("style_hair_$sex")
            val colours = EnumDefinitions.get("colour_hair")
            interfaceOptions.unlockAll(id, "styles", 0 until styles.length * 2)
            interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
            set("makeover_hair", body.getLook(BodyPart.Hair))
            set("makeover_beard", body.getLook(BodyPart.Beard))
            set("makeover_colour_hair", body.getColour(BodyColour.Hair))
        }

        interfaceOption(id = "hairdressers_salon:style_*") {
            set("makeover_facial_hair", it.component == "style_beard")
        }

        interfaceOption(id = "hairdressers_salon:styles") { (_, itemSlot) ->
            val beard = get("makeover_facial_hair", false)
            val type = if (beard) "beard" else "hair"
            val key = "look_${type}_$sex"
            val value = if (beard) {
                EnumDefinitions.get(key).getInt(itemSlot / 2)
            } else {
                EnumDefinitions.getStruct(key, itemSlot / 2, "body_look_id")
            }
            set("makeover_$type", value)
        }

        interfaceOption(id = "hairdressers_salon:colours") { (_, itemSlot) ->
            set("makeover_colour_hair", EnumDefinitions.get("colour_hair").getInt(itemSlot / 2))
        }

        interfaceClosed("hairdressers_salon") {
            softTimers.stop("dressing_room")
        }

        interfaceOption("Confirm", "hairdressers_salon:confirm") {
            body.setLook(BodyPart.Hair, get("makeover_hair", 0))
            body.setLook(BodyPart.Beard, get("makeover_beard", 0))
            body.setColour(BodyColour.Hair, get("makeover_colour_hair", 0))
            flagAppearance()
            closeMenu()
            npc<Happy>(
                "hairdresser",
                if (male) {
                    listOf("An excellent choice, sir.", "Mmm... very distinguished!")
                } else {
                    listOf("A marvellous choice. You look splendid!", "It really suits you!")
                }.random(),
            )
        }
    }

    suspend fun Player.startHairdressing() {
        closeDialogue()
        if (equipped(EquipSlot.Weapon).isNotEmpty() || equipped(EquipSlot.Shield).isNotEmpty()) {
            npc<Scared>("I don't feel comfortable cutting hair when you are wielding something. Please remove what you are holding first.")
            return
        }
        if (equipped(EquipSlot.Hat).isNotEmpty()) {
            npc<Sad>("I can't cut your hair with that on your head.")
            return
        }
        openDressingRoom("hairdressers_salon")
    }
}
