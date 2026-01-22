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

class MonkeyShopkeeper : Script {

    init {
        npcOperate("Talk-to", "hamab,ifaba,tutab") { (npc) ->
            val amulet = equipped(EquipSlot.Amulet)
            val shopName = npc.def["shop", ""]  // Gets shop name from NPC def

            if (amulet.id == "monkeyspeak_amulet") {
                npc<Shifty>("Would you like to see my wares?")
                choice {
                    option("Yes, please.") {
                        player<Shifty>("Yes, please.")
                        openShop(shopName)
                    }
                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }
                }
            } else {
                npc<Shifty>(npc.id, "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "hamab,ifaba,tutab") { (npc) ->
            val amulet = equipped(EquipSlot.Amulet)
            val shopName = npc.def["shop", ""]

            if (amulet.id == "monkeyspeak_amulet") {
                openShop(shopName)
            } else {
                npc<Shifty>(npc.id, "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}