package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Tile

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

        takeable("cup_of_tea") { item, telegrab ->
            if (item.tile != Tile(3271, 3413)) {
                return@takeable item.id
            }
            if (telegrab) {
                npc<Angry>("tea_seller", "Hey! Put that back! Those are for display only!")
            } else {
                npc<Angry>("tea_seller", "Hey! Put that back! Those are for display only!")
            }
            null
        }
    }
}
