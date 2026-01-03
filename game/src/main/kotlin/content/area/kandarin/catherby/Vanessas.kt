package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Vanessas : Script {

    init {
        npcOperate("Talk-to", "vanessa") {
            npc<Neutral>("Hello. How can I help you?")

            choice {
                option("What are you selling?") {
                    openShop("vanessas_farming_shop")
                }

                option("Can you give me any Farming advice?") {
                    player<Neutral>("Can you give me any Farming advice?")
                    npc<Neutral>("Yes - ask a gardener.")
                }

                option("I'm okay, thank you.") {
                    player<Neutral>("I'm okay, thank you.")
                }
            }
        }

        npcOperate("Trade", "vanessa") {
            openShop("vanessas_farming_shop")
        }
    }
}
