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

class Tutab : Script {

    init {
        npcOperate("Talk-to", "tutab") {
            val amulet = equipped(EquipSlot.Amulet)

            if (amulet.id == "monkeyspeak_amulet") {
                // Fixed name to "tutab" and matched the official Wiki transcript
                npc<Shifty>("tutab", "Would you like to buy or sell some magical items?")

                choice {
                    option("Yes, please.") {
                        player<Shifty>("Yes, please.")
                        closeDialogue() // Clears the chatbox so the shop opens cleanly
                        openShop("tutabs_magical_market")
                    }
                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }
                }
            } else {
                // Monkey dialogue when not wearing the amulet
                npc<Shifty>("tutab", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "tutab") {
            val amulet = equipped(EquipSlot.Amulet)
            if (amulet.id == "monkeyspeak_amulet") {
                openShop("tutabs_magical_market")
            } else {
                npc<Shifty>("tutab", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}