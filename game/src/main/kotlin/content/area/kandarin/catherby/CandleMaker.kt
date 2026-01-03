package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class CandleMaker : Script {

    init {
        npcOperate("Talk-to", "candle_maker") {
            npc<Neutral>("Hi! Would you be interested in some of my fine candles?")

            choice {
                option("Yes please.") {
                    player<Neutral>("Yes please.")
                    openShop("candle_shop")
                }

                option("No thank you.") {
                    player<Neutral>("No thank you.")
                    // Ends dialogue naturally
                }
            }
        }
    }

    // TODO: add Merlin's Crystal quest as that how you got black candle from the shop
}
