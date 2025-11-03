package content.area.asgarnia.falador

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Hairdresser : Script {

    val enums: EnumDefinitions by inject()

    init {
        npcOperate("Talk-to", "hairdresser") {
            npc<Pleased>("Good afternoon ${if (male) "sir" else "madam"}. In need of a haircut${if (male) " or shave" else ""} are we?")
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

        interfaceOpen("hairdressers_salon") { id ->
            interfaces.sendText(id, "confirm_text", "Change")
            val styles = enums.get("style_hair_$sex")
            val colours = enums.get("colour_hair")
            interfaceOptions.unlockAll(id, "styles", 0 until styles.length * 2)
            interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
            set("makeover_hair", body.getLook(BodyPart.Hair))
            set("makeover_beard", body.getLook(BodyPart.Beard))
            set("makeover_colour_hair", body.getColour(BodyColour.Hair))
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

        interfaceClose("hairdressers_salon") {
            softTimers.stop("dressing_room")
        }

        interfaceOption("Confirm", "confirm", "hairdressers_salon") {
            player.body.setLook(BodyPart.Hair, player["makeover_hair", 0])
            player.body.setLook(BodyPart.Beard, player["makeover_beard", 0])
            player.body.setColour(BodyColour.Hair, player["makeover_colour_hair", 0])
            player.flagAppearance()
            player.closeMenu()
            npc<Happy>(
                "hairdresser",
                if (player.male) {
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
            npc<Afraid>("I don't feel comfortable cutting hair when you are wielding something. Please remove what you are holding first.")
            return
        }
        if (equipped(EquipSlot.Hat).isNotEmpty()) {
            npc<Upset>("I can't cut your hair with that on your head.")
            return
        }
        openDressingRoom("hairdressers_salon")
    }
}
