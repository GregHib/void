package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Dommik : Script {

    init {
        npcOperate("Talk-to", "dommik") {
            npc<Happy>("Would you like to buy some crafting equipment?")
            choice {
                option<Idle>("No thanks; I've got all the Crafting equipment I need.") {
                    npc<Happy>("Okay. Fare well on your travels.")
                }
                option<Idle>("Let's see what you've got, then.") {
                    openShop("dommiks_crafting_store")
                }
            }
        }

        npcOperate("Trade", "dommik") {
            openShop("dommiks_crafting_store")
        }
    }
}
