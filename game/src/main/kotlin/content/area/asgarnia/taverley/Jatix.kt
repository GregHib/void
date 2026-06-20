package content.area.asgarnia.taverley

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Jatix : Script {

    init {
        npcOperate("Talk-to", "jatix") {
            npc<Neutral>("Hello, how can I help you?")
            choice {
                option("What are you selling?") {
                    openShop("jatixs_herblore_shop")
                }
                option<Disheartened>("You can't; I'm beyond help.")
                option<Neutral>("I'm okay, thank you.")
            }
        }
    }
}
