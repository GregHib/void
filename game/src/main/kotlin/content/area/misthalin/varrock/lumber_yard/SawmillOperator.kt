package content.area.misthalin.varrock.lumber_yard

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World

class SawmillOperator : Script {
    init {
        npcApproach("Talk-to", "sawmill_operator") { (target) ->
            approachRange(2)
            if (!World.members) {
                statement("The sawmill operator pays you no attention, it doesn't seem like he'll help you.")
                return@npcApproach
            }
            npc<Quiz>("Hello there. Do you want me to make some planks for you? I can make planks from wood, oak, teak and mahogany logs. Or would you like to buy some other housing supplies?")
            choice {
                option<Neutral>("Yes, please make me some planks.") {
                    open("sawmill")
                }
                option<Quiz>("Can I buy some housing supplies?") {
                    npc<Happy>("Of course!")
                    openShop(target.def["shop"])
                }
                if (quest(GERTRUDES_CAT) == "found_the_boys" && get("gertrudes_cat_know_where_fluffs_is", false)) {
                    option<Neutral>("I have some questions about a cat.") {
                        npc<Quiz>("A cat? What makes you ask about that?")
                        player<Neutral>("I'm looking for a cat named Fluffs and my sources tell me she may be in your sawmill.")
                        npc<Confused>("Your sources? Who's been talking about my sawmill behind my back? This is a business. We don't have animals on the premises.")
                    }
                }
            }
        }

        npcApproach("Buy-plank", "sawmill_operator") {
            approachRange(2)
            open("sawmill")
        }

        npcApproach("Trade", "sawmill_operator") { (target) ->
            approachRange(2)
            openShop(target.def["shop"])
        }
    }
}
