package content.area.karamja.ape_atroll

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Ifaba : Script {

    init {
        npcOperate("Talk-to", "ifaba") {
            val amulet = equipped(EquipSlot.Amulet)

            if (amulet.id == "monkeyspeak_amulet") {
                // Corrected to "ifaba" and updated text to match the General Store transcript
                npc<Shifty>("ifaba", "Would you like to buy or sell anything?")

                choice {
                    option("Yes, please.") {
                        player<Shifty>("Yes, please.")
                        closeDialogue() // Clears the dialogue so the shop opens correctly
                        openShop("ifabas_general_store")
                    }
                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }
                }
            } else {
                // Standard monkey dialogue without the amulet
                npc<Shifty>("ifaba", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "ifaba") {
            val amulet = equipped(EquipSlot.Amulet)
            if (amulet.id == "monkeyspeak_amulet") {
                openShop("ifabas_general_store")
            } else {
                npc<Shifty>("ifaba", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}