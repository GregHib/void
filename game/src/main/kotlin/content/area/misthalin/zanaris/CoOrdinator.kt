package content.area.misthalin.zanaris

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class CoOrdinator : Script {
    init {
        npcOperate("Talk-to", "co_ordinator") {
            player<Quiz>("Hello, what are you doing?")
            when (random.nextInt(4)) {
                0 -> npc<Confused>("Sorry, I don't have time to stop, I need to send a Weather Fairy off to Etceteria!")
                1 -> npc<Confused>("Sorry, I don't have time for idle chit-chat, I need to send an Autumn Fairy off to Burthorpe!")
                2 -> npc<Confused>("Sorry, I don't have time for idle chit-chat, I need to find a Winter Fairy to send to Trollheim!")
                else -> npc<Confused>("Sorry, I don't have time for idle chit-chat, I need to send a fairy to get little Freddies tooth!")
            }
        }
    }
}
