package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class Ibis : Script {
    init {
        npcOperate("Interact", "ibis_familiar") {
            if (inventory.contains("raw_shark", 2)) {
                npc<Neutral>("Can I look after those sharks for you?")
                player<Happy>("I don't know. Would you eat them?")
                npc<Neutral>("Yes! Ooops...")
                player<Happy>("I think I'll hang onto them myself for now.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("I'm the best fisherman ever!")
                    player<Happy>("Where is your skillcape to prove it, then?")
                    npc<Neutral>("At home...")
                    player<Happy>("I'll bet it is.")
                }
                1 -> {
                    npc<Neutral>("I'll bet it is.")
                    player<Happy>("I like to fish!")
                }
                2 -> {
                    npc<Neutral>("I'll bet it is.")
                    player<Happy>("I like to fish!")
                }
                3 -> {
                    npc<Neutral>("Hey, where are we?")
                    player<Happy>("What do you mean?")
                    npc<Neutral>("I just noticed we weren't fishing.")
                    player<Happy>("Well, we can't fish all the time.")
                }
            }
        }
    }
}
