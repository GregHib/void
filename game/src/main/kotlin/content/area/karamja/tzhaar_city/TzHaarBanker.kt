package content.area.karamja.tzhaar_city

import content.area.karamja.tzhaar_city.TzHaar.whatDidYouCallMe
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.name

class TzHaarBanker : Script {

    init {
        npcOperate("Talk-to", "tzhaar_ket_zuh") {
            npc<Quiz>("Can I help you JalYt-${TzHaar.caste(this)}-$name?")
            choice {
                option("I'd like to access my bank account please.", block = { open("bank") })
                whatDidYouCallMe(it.target)
                option("I'd like to check my PIN settings.", block = { open("bank_pin") })
                option("I'd like to see my collection box.", block = { open("collection_box") })
                option("I'd like to see my Returned Items box.", block = { open("returned_items") })
            }
        }
        npcOperate("Bank", "tzhaar_ket_zuh") { open("bank") }
        npcOperate("Collect", "tzhaar_ket_zuh") { open("collection_box") }
    }
}
