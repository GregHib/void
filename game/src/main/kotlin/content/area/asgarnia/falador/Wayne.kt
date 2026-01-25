package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Wayne : Script {

    init {
        npcOperate("Talk-to", "wayne") {
            npc<Neutral>(" Welcome to Wayne's Chains. <br>Do you wanna buy or sell some chainmail?")
            choice {
                option("Yes please.") {
                    player<Neutral>("Yes please.")
                    openShop("waynes_chains_chainmail_specialist")
                }
                option("No, thanks.") {
                    player<Neutral>("No, thanks.")
                }
            }
        }
    }
}