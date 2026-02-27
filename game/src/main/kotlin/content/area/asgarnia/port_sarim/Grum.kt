package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Grum : Script {
    init {
        npcOperate("Talk-to", "grum") { (target) ->
            npc<Happy>("Would you like to buy or sell some gold jewellery?")
            choice {
                option<Happy>("Yes please.") {
                    openShop(target.def["shop"])
                }
                option<Happy>("No, I'm not that rich.") {
                    npc<Angry>("Get out then! We don't want any riff-raff in here.")
                }
            }
        }
    }
}
