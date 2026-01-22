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

class Solihib : Script {

    init {
        npcOperate("Talk-to", "solihib") {
            val amulet = equipped(EquipSlot.Amulet)

            if (amulet.id == "monkeyspeak_amulet") {
                npc<Shifty>("solihib", "Would you like to buy or sell some foodstuffs?")

                choice {
                    option("Yes, please.") {
                        player<Shifty>("Yes, please.")
                        openShop("solihibs_food_stall")
                    }
                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }
                }
            } else {
                npc<Shifty>("solihib", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "solihib") {
            val amulet = equipped(EquipSlot.Amulet)
            if (amulet.id == "monkeyspeak_amulet") {
                openShop("solihibs_food_stall")
            } else {
                npc<Shifty>("solihib", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}