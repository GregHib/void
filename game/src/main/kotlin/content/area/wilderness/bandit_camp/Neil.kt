package content.area.wilderness.bandit_camp

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Neil : Script {

    init {
        npcOperate("Talk-to", "neil") { (target) ->
            wildernessCapeShop(target.def["shop"])
        }
    }

    private suspend fun Player.wildernessCapeShop(shop: String) {
        npc<Happy>("Hello there! Are you interested in buying one of my special capes?")
        shopChoice(shop)
    }

    private suspend fun Player.shopChoice(shop: String) {
        choice {
            option<Quiz>("What do team capes do?") {
                npc<Happy>("If you and your friends all wear the same team cape, you'll be less likely to attack your friends by accident, and you'll find it easier to attack everyone else.")
                npc<Happy>("They're very useful in Clan Wars and other player-vs-player combat areas where you might come across friends you don't want to harm.")
                npc<Quiz>("So would you like to buy one?")
                confirmPurchase(shop)
            }
            option("Yes please!") {
                openShop(shop)
            }
            option<Neutral>("No thanks.") {
            }
        }
    }

    private suspend fun Player.confirmPurchase(shop: String) {
        choice {
            option("Yes please!") {
                openShop(shop)
            }
            option<Neutral>("No thanks.") {
            }
        }
    }
}
