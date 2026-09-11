package content.area.tirannwn.lletya

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class LletyaShopkeepers : Script {

    init {
        npcOperate("Talk-to", "eudav,gethin") { (target) ->
            npc<Happy>("Can I help you at all?")
            choice {
                option<Quiz>("Yes please. What are you selling?") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.")
            }
        }

        npcOperate("Talk-to", "oronwen") { (target) ->
            npc<Happy>("Hello, can I help?")
            choice {
                option<Quiz>("Yes please. What are you selling?") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.")
            }
        }

        npcOperate("Talk-to", "dalldav") { (target) ->
            npc<Happy>("Can I help you at all?")
            choice {
                option<Quiz>("Yes please. What are you selling?") {
                    openShop(target.def["shop"])
                }
                option<Quiz>("Why do you sell this stuff? The Crystal Bow is so much better.") {
                    npc<Happy>("We keep all these old toys to train our children with, but if people will part with coins for them, then they are theirs!")
                }
                option<Neutral>("No thanks.")
            }
        }
    }
}
