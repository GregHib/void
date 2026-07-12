package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class Faruq : Script {

    private val shop = "faruqs_tools_for_games"

    init {
        npcOperate("Talk-to", "faruq") {
            npc<Amazed>("Hello! Have you come to sample my marvellous wares?")
            choice {
                option("Yes, I'd like to see what you have.") {
                    openShop(shop)
                }
                option("Perhaps. What are they for?") {
                    player<Neutral>("Perhaps. Your stall has some odd-looking stuff; what are they for?")
                    npc<Happy>("I sell them the tools to keep track of time, mark out places and routes, decide things randomly, even to hold great ballots of their group.")
                    choice {
                        option("Let me see, then.") {
                            openShop(shop)
                        }
                        option("These tools, are they complicated?") {
                            player<Neutral>("These tools, are they complicated?")
                            npc<Neutral>("No, $name, they are not complicated.")
                            npc<Neutral>("I have a book that explains them, should you need.")
                            openShop(shop)
                        }
                        option("I don't think this is for me.") {
                            player<Neutral>("I don't think this is for me.")
                            npc<Neutral>("That is a shame. I shall be here if you change your mind.")
                        }
                    }
                }
                option("No, thanks.") {
                    player<Neutral>("No, thanks.")
                }
            }
        }

        npcOperate("Trade", "faruq") {
            openShop(shop)
        }
    }
}
