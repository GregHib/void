package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Rometti : Script {
    init {
        npcOperate("Talk-to", "rometti") { (target) ->
            player<Neutral>("Hello.")
            npc<Neutral>("Hello traveller. Have a look at my latest range of gnome fashion. Rometti is the ultimate label in gnome high society.")
            player<Bored>("Really.")
            npc<Neutral>("Pastels are all the rage this season.")
            choice {
                option<Neutral>("I've no time for fashion.") {
                    npc<Neutral>("Hmm... I did wonder.")
                }
                option<Neutral>("OK then, let's have a look.") {
                    openShop(target.def["shop"])
                }
            }
        }
    }
}
