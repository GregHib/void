package content.area.kharidian_desert.nardah

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Zahur : Script {
    init {
        npcOperate("Talk-to", "zahur") { (target) ->
            if (get("spoke_nardah_herblist", false)) {
                npc<Happy>("Hello again. Do you need anything?")
            } else {
                npc<Happy>("Hi there. Do you need anything? I can clean herbs for a fee of 200 coins per herb. I can clean all of your herbs at once, or I can do individual herbs if you show them to me.")
                set("spoke_nardah_herblist", true)
                npc<Happy>("I can also decant your potion vials to into different doses. That service is free so long as you have the empty vials needed. If not, it's just a few coins.")
            }
            choice {
                cleanHerbs()
                decantPotions()
                whyPay()
                anotherTime()
            }
        }
    }

    private fun ChoiceOption.whyPay() {
        option<Quiz>("Why would I pay for you to clean herbs?") {
            npc<Happy>("Many of my customers wish to sell the herbs which they can't clean to other adventurers. They might hope for a better price by doing this.")
            choice {
                cleanHerbs()
                decantPotions()
                anotherTime()
            }
        }
    }

    private fun ChoiceOption.anotherTime() {
        option<Shifty>("Maybe another time.")
    }

    private fun ChoiceOption.decantPotions() {
        option("Please decant my potions.") {
            npc<Sad>("I didn't find anything that I could decant.")
            message("<purple>Not yet implemented.") // TODO
        }
    }

    private fun ChoiceOption.cleanHerbs() {
        option("Please clean all my herbs.") {
            npc<Sad>("I didn't find anything that I could clean.")
            message("<purple>Not yet implemented.") // TODO
        }
    }
}
