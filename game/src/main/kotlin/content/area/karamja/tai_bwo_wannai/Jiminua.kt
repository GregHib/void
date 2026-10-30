package content.area.karamja.tai_bwo_wannai

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Jiminua : Script {
    init {
        npcOperate("Talk-to", "jiminua_tai_bwo_wannai") { (target) ->
            npc<Happy>("Welcome to the Jungle Store. Can I help you at all?")
            choice {
                option<Neutral>("Yes please. What are you selling?") {
                    npc<Neutral>("Take yourself a good look.")
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.")
            }
        }
    }
}
