package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class VarrockSwordShop : Script {
    init {
        npcOperate("Talk-to", "sword_shop_assistant_varrock,sword_shopkeeper_varrock") {
            npc<Happy>("Hello, bold adventurer! Can I interest you in some swords?")
            choice {
                option<Neutral>("Yes, please!") {
                    openShop("varrock_sword_shop")
                }
                option<Neutral>("No, I'm okay for swords right now.") {
                    npc<Happy>("Come back if you need any.")
                }
            }
        }
    }
}
