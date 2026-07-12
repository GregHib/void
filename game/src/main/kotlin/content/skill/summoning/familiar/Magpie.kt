package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Magpie : Script {
    init {
        npcOperate("Interact", "magpie_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("There's nowt gannin on here...")
                    player<Happy>("Err...sure? Maybe?")
                    player<Happy>("It seems upset, but what is it saying?")
                }
                1 -> {
                    npc<Neutral>("Howway, let's gaan see what's happenin' in toon.")
                    player<Happy>("What? I can't understand what you're saying.")
                }
                2 -> {
                    npc<Neutral>("Are we gaan oot soon? I'm up fer a good walk me.")
                    player<Happy>("That...that was just noise. What does that mean?")
                }
                3 -> {
                    npc<Neutral>("Ye' been plowdin' i' the claarts aall day.")
                    player<Happy>("What? That made no sense.")
                }
            }
        }
    }
}
