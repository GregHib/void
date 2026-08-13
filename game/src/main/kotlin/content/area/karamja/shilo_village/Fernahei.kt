package content.area.karamja.shilo_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Fernahei : Script {
    init {
        npcOperate("Talk-to", "fernahei_shilo_village") { (target) ->
            npc<Happy>("Welcome to Fernahei's Fishing Shop Bwana! Would you like to see my items?")
            choice {
                option<Neutral>("Yes please!") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No, but thanks for the offer.") {
                    npc<Happy>("That's fine and thanks for your interest.")
                }
            }
        }
    }
}
