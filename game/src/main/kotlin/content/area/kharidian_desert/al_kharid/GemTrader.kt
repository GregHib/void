package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class GemTrader : Script {

    private val shop = "gem_trader"

    init {
        npcOperate("Talk-to", "gem_trader") {
            npc<Neutral>("Good day to you, traveller. Would you be interested in buying some gems?")
            choice {
                option<Neutral>("Yes, please.") {
                    openShop(shop)
                }
                option("No, thank you.") {
                    player<Neutral>("No, thank you.")
                    npc<Neutral>("Eh, suit yourself.")
                }
            }
        }
    }
}
