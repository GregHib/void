package content.area.karamja.tzhaar_city

import content.area.karamja.tzhaar_city.TzHaar.whatDidYouCallMe
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class TzHaarShops : Script {

    init {
        npcOperate("Talk-to", "tzhaar_hur_tel,tzhaar_hur_lek,tzhaar_mej_roh") { (target) ->
            npc<Quiz>("Can I help you JalYt-${TzHaar.caste(this)}-$name?")
            choice {
                option("What do you have to trade?") {
                    openShop(target.def["shop", ""])
                }
                whatDidYouCallMe(target)
                option<Neutral>("No I'm fine thanks.")
            }
        }
    }
}
