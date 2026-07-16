package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class EvilTurnip : Script {
    init {
        npcOperate("Interact", "evil_turnip_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("So, how are you feeling?")
                    npc<Neutral>("My roots feel hurty. I thinking it be someone I eated.")
                    player<Happy>("You mean someTHING you ate?")
                    npc<Neutral>("Hur hur hur. Yah, sure, why not.")
                }
                1 -> {
                    npc<Neutral>("Hur hur hur...")
                    player<Happy>("Well, as sinister as it's chuckling is, at least it's happy. That's a good thing, right?")
                }
                2 -> {
                    npc<Neutral>("When we gonna fighting things, boss?")
                    player<Happy>("Soon enough.")
                    npc<Neutral>("Hur hur hur. I gets the fighting.")
                }
                3 -> {
                    npc<Neutral>("I are turnip hear me roar! I too deadly to ignore.")
                    player<Happy>("I'm glad it's on my side... and not behind me.")
                }
            }
        }
    }
}
