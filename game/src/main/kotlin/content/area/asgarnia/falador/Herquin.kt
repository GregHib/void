package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Herquin : Script {

    init {
        npcOperate("Talk-to", "herquin") {
            choice {
                option<Quiz>("Do you wish to trade?") {
                    npc<Happy>("Why, yes, this is a jewel shop after all.")
                    openShop("herquins_gems")
                }
                option<Neutral>("Sorry, I don't want to talk to you, actually.") {
                    npc<Angry>("Huh, charming.")
                }
            }
        }
    }
}
