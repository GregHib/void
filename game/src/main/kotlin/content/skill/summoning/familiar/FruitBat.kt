package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class FruitBat : Script {
    init {
        npcOperate("Interact", "fruit_bat_familiar") {
            if (inventory.count("papaya_fruit") > 3) {
                npc<Neutral>("Squeek squeek-a-squeek squeeek? (Can I have a papaya?)")
                player<Happy>("No, I have a very specific plan for them.")
                npc<Neutral>("Squeek? (What?)")
                player<Happy>("I was just going to grate it over some other vegetables and eat it. Yum.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Squeek-a-squeek squeek? (How much longer do you want me for?)")
                    player<Happy>("I don't really know at the moment, it all depends what I want to do today.")
                }
                1 -> {
                    npc<Neutral>("Squeak squeek-a-squeak. (This place is fun!)")
                    player<Happy>("Glad you think so!")
                }
                2 -> {
                    npc<Neutral>("Squeek squeek squeek-a-squeek? (Where are we going?)")
                    player<Happy>("Oh, we're likely to go to a lot of places today.")
                }
                3 -> {
                    npc<Neutral>("Squeek squeek-a-squeek squeek? (Can you smell lemons?)")
                    player<Happy>("No, why do you ask?")
                    npc<Neutral>("Squeak-a-squeak squeek. (Must just be thinking about them.)")
                }
            }
        }
    }
}
