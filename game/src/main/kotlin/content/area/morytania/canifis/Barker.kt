package content.area.morytania.canifis

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Barker : Script {
    init {
        npcOperate("Talk-to", "barker") { (target) ->
            npc<Neutral>("You are looking for clothes, yes? You look at my products! I have very many nice clothes, yes?")
            choice {
                option<Neutral>("Yes, please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.") {
                    npc<Sad>("Unfortunate for you, yes? Many bargains, won't find elsewhere!")
                }
            }
        }
    }
}
