package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.type.random

class Clay : Script {
    init {
        npcOperate("Interact", "clay_familiar_class_*_familiar") {
            if (beastOfBurden.items.count { it.isNotEmpty() } > 0) {
                player<Happy>("How are you getting on with the load?")
                npc<Neutral>("Rumble. (Just fine, master.)")
                player<Happy>("Don't go dropping it, okay?")
                npc<Neutral>("Rumble. (I'll try my very best, master.)")
                return@npcOperate
            }
            when (random.nextInt(3)) {
                0 -> {
                    player<Happy>("What is it like to be made out of sacred clay?")
                    npc<Neutral>("Rumble... (I do not understand the question...)")
                    player<Happy>("Can you at least tell me how you feel?")
                    npc<Neutral>("Rumble! (I am happy as long as I can serve you, master!)")
                }
                1 -> {
                    player<Happy>("They're attacking!")
                    npc<Neutral>("Rumble! (Fear not, master, for I'll protect you!)")
                    player<Happy>("I'm glad you're here!")
                }
                2 -> {
                    player<Happy>("Hey!")
                    npc<Neutral>("Rumble? (Yes, master?)")
                    player<Happy>("Actually, I probably don't want to be talking to you. It's kind of dangerous here...")
                    npc<Neutral>("Rumble... (As You wish...)")
                }
            }
        }
    }
}
