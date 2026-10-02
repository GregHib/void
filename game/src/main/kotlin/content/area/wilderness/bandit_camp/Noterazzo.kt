package content.area.wilderness.bandit_camp

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Noterazzo : Script {

    init {
        npcOperate("Talk-to", "noterazzo") { (target) ->
            dialogue(target.def["shop"])
        }
    }

    private suspend fun Player.dialogue(shop: String) {
        npc<Shifty>("Hey wanna trade? I'll give the best deals you can find.")
        choice {
            option("Yes please.") {
                openShop(shop)
            }
            option<Neutral>("No thanks.") {
            }
            option<Quiz>("How can you afford to give such good deals?") {
                npc<Shifty>("The general stores in Asgarnia and Misthalin are heavily taxed. It really makes it hard for them to run an effective business. For some reason taxmen don't visit my store.")
            }
        }
    }
}
