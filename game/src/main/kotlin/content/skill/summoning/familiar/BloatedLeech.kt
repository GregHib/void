package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class BloatedLeech : Script {
    init {
        npcOperate("Interact", "bloated_leech_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("I'm afraid it's going to have to come off, $name.")
                    player<Happy>("What is?")
                    npc<Neutral>("Never mind. Trust me, I'm almost a doctor.")
                    player<Happy>("I think I'll get a second opinion.")
                }
                1 -> {
                    npc<Neutral>("You're in a critical condition.")
                    player<Happy>("Is it terminal?")
                    npc<Neutral>("Not yet. Let me get a better look and I'll see what I can do about it.")
                    player<Happy>("There are two ways to take that...and I think I'll err on the side of caution.")
                }
                2 -> {
                    npc<Neutral>("Let's get a look at that brain of yours.")
                    player<Happy>("What? My brains stay inside my head, thanks.")
                    npc<Neutral>("That's ok, I can just drill a hole.")
                    player<Happy>("How about you don't and pretend you did?")
                }
                3 -> {
                    npc<Neutral>("I think we're going to need to operate.")
                    player<Happy>("I think we can skip that for now.")
                    npc<Neutral>("Who's the doctor here?")
                    player<Happy>("Not you.")
                    npc<Neutral>("I may not be a doctor, but I'm keen. Does that not count?")
                    player<Happy>("In most other fields, yes; in medicine, no.")
                }
            }
        }
    }
}
