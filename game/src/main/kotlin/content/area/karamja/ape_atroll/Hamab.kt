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

class Hamab : Script {

    init {
        npcOperate("Talk-to", "hamab") {
            val amulet = equipped(EquipSlot.Amulet)

            if (amulet.id == "monkeyspeak_amulet") {
                // Explicitly named "hamab" to fix the 'null' error
                npc<Shifty>("hamab", "Would you like to buy or sell some crafting materials?")

                choice {
                    option("Yes, please.") {
                        player<Shifty>("Yes, please.")
                        closeDialogue() // Clears dialogue so shop opens correctly
                        openShop("hamabs_crafting_emporium")
                    }
                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }
                }
            } else {
                // Monkey dialogue when not wearing the amulet
                npc<Shifty>("hamab", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "hamab") {
            val amulet = equipped(EquipSlot.Amulet)
            if (amulet.id == "monkeyspeak_amulet") {
                openShop("hamabs_crafting_emporium")
            } else {
                npc<Shifty>("hamab", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}
