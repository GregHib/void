package content.area.karamja.ape_atroll

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class MonkeyShopkeeper : Script {

    init {
        npcOperate("Talk-to", "hamab,ifaba,tutab,solihib") { (npc) ->
            val amulet = equipped(EquipSlot.Amulet)
            val shopName = npc.def["shop", ""]

            if (amulet.id == "monkeyspeak_amulet") {
                npc<Shifty>(
                    when (npc.id) {
                        "ifaba" -> "Would you like to buy or sell anything?"
                        "hamab" -> "Would you like to buy or sell some crafting materials?"
                        "solihib" -> "Would you like to buy or sell some foodstuffs?"
                        else -> "Would you like to buy or sell some magical items?"
                    },
                )
                choice {
                        option<Quiz>("Yes, please.") {
                        openShop(shopName)
                    }
                        option<Quiz>("No, thanks.") {
                    }
                }
            } else {
                npc<Shifty>("Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "hamab,ifaba,tutab,solihib") { (npc) ->
            val amulet = equipped(EquipSlot.Amulet)
            val shopName = npc.def["shop", ""]

            if (amulet.id == "monkeyspeak_amulet") {
                openShop(shopName)
            } else {
                npc<Shifty>("Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}
