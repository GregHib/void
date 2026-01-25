package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Flynn : Script {

    init {
        npcOperate("Talk-to", "flynn") {
            npc<Neutral>("Hello. Do you want to buy or sell any maces?")
            choice {
                option("No, thanks.") {
                    player<Neutral>("No, thanks.")
                }
                option("Well, I'll have a look, at least.") {
                    player<Neutral>("Well, I'll have a look, at least.")
                    openShop("flynns_mace_market")
                }
            }
        }
    }
}