package content.area.kharidian_desert.nardah

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Kazemde : Script {
    init {
        npcOperate("Talk-to", "kazemde") { (target) ->
            npc<Happy>("Can I help you at all?")
            choice {
                option("Yes please. What are you selling?") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thank you.")
            }
        }
    }
}
