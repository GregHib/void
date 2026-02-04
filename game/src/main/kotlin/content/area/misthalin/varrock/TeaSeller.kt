package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class TeaSeller : Script {

    init {
        npcOperate("Talk-to", "tea_seller") {
            npc<Happy>("Greetings! Are you in need of refreshment?")
            choice {
                option<Neutral>("Yes please.") {
                    openShop("ye_olde_tea_shoppe")
                }
                option<Neutral>("No thanks.") {
                    npc<Happy>("Well, if you're sure. You know where to come if you do!")
                }
                option<Neutral>("What are you selling?") {
                    npc<Happy>("Only the most delicious infusion of the leaves of the tea plant. Grown in the exotic regions of this world.<br>Buy yourself a cup!")
                }
            }
        }
    }
}
