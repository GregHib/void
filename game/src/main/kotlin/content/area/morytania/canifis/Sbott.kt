package content.area.morytania.canifis

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.inv.inventory

class Sbott : Script {
    init {
        npcOperate("Talk-to", "sbott") { (target) ->
            npc<Neutral>("Hello stranger. Would you like me to tan any hides for you?")
            npc<Happy>("Soft leather - 2 gp per hide<br>Hard leather - 5 gp per hide<br>Snakeskins - 25 gp per hide<br>Dragon leather - 45 gp per hide.")
            choice("What would you like to say?") {
                val hides = inventory.items.any { it.id == "cowhide" || it.id.startsWith("snake_hide") || it.id.endsWith("dragonhide") }
                if (hides) {
                    option<Neutral>("Yes please.") {
                        set("tanner", "sbott")
                        open("tanner")
                    }
                }
                option("Why are you so expensive?") {
                    player<Quiz>("Why are you so expensive? The tanner in Al Kharid is almost half the price!")
                    npc<Neutral>("Hey, I charge more because I'm worth it! I deal in bulk, and I work extremely quickly. You'll see for yourself!")
                    npc<Neutral>("You got a lot of hides you want tanning quickly? I'm your guy!")
                    npc<Quiz>("So you got hides for me to tan, or are you just gonna bust my chops about prices all day?")
                    player<Neutral>("No thanks, I haven't any hides.")
                    npc<Neutral>("Fair enough. I can't tan what you don't bring me.")
                }
                if (hides) {
                    option<Neutral>("No thanks, I'm not interested.") {
                        npc<Neutral>("Okay; you change your mind, you come see me. I'm your guy!")
                    }
                } else {
                    option<Neutral>("No thanks, I haven't any hides.") {
                        npc<Neutral>("Fair enough. I can't tan what you don't bring me.")
                    }
                }
            }
        }

        npcOperate("Trade", "sbott") {
            set("tanner", "sbott")
            open("tanner")
        }
    }
}
