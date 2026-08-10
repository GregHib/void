package content.minigame.mage_arena

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Lundail : Script {

    init {
        npcOperate("Talk-to", "lundail") {
            npc<Quiz>("How can I help you, brave adventurer?")
            choice {
                option<Quiz>("What are you selling?") {
                    npc<Neutral>("I sell rune stones. I've got some good stuff, some really powerful little rocks. Take a look.")
                    openShop("lundails_arena_side_rune_shop")
                }
                option<Quiz>("What's that big old building above us?") {
                    npc<Neutral>("That, my friend, is the mage battle arena. Top mages come from all over Gielinor to compete in the arena.")
                    player<Neutral>("Wow.")
                    npc<Sad>("Few return, most get fried, hence the smell.")
                    player<Neutral>("Hmmm.. I did notice.")
                }
            }
        }
    }
}
