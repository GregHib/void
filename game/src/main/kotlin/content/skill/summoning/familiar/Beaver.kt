package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class Beaver : Script {
    init {
        npcOperate("Interact", "beaver_familiar") {
            if (inventory.items.any { it.id.endsWith("logs") }) {
                npc<Neutral>("'Ere, you 'ave ze logs, now form zem into a mighty dam!")
                player<Happy>("Well, I was thinking of burning, selling, or fletching them.")
                npc<Neutral>("Sacre bleu! Such a waste.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Vot are you doing 'ere when we could be logging and building mighty dams, alors?")
                    player<Happy>("Why would I want to build a dam again?")
                    npc<Neutral>("Why vouldn't you want to build a dam again?")
                    player<Happy>("I can't argue with that logic.")
                }
                1 -> {
                    npc<Neutral>("Pardonnez-moi - you call yourself a lumberjack?")
                    player<Happy>("No")
                    npc<Neutral>("Carry on zen.")
                }
                2 -> {
                    npc<Neutral>("Paul Bunyan 'as nothing on moi!")
                    player<Happy>("Except several feet in height, a better beard, and opposable thumbs.")
                    npc<Neutral>("What was zat?")
                    player<Happy>("Nothing.")
                }
                3 -> {
                    npc<Neutral>("Zis is a fine day make some lumber.")
                    player<Happy>("That it is!")
                    npc<Neutral>("So why are you talking to moi? Get chopping!")
                }
            }
        }
    }
}
