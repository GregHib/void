package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class AlbinoRat : Script {
    init {
        npcOperate("Interact", "albino_rat_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Hey boss, we going to do anything wicked today?")
                    player<Happy>("Well, I don't know why we would: I tend not to go around being wicked.")
                    npc<Neutral>("Not even a little?")
                    player<Happy>("Well there was that one time... I'm sorry, no wickedness today.")
                    npc<Neutral>("Awwwwww...")
                }
                1 -> {
                    npc<Neutral>("Hey boss, can we go and loot something now?")
                    player<Happy>("Well, what did you have in mind?")
                    npc<Neutral>("I dunno - where are we headed?")
                    player<Happy>("I hadn't decided yet.")
                    npc<Neutral>("When we get there, let's loot something nearby!")
                    player<Happy>("Sounds like a plan, certainly.")
                }
                2 -> {
                    npc<Neutral>("So what we up to today, boss?")
                    player<Happy>("Oh I'm sure we'll find something to occupy our time.")
                    npc<Neutral>("Let's go robbin' graves again!")
                    player<Happy>("What do you mean 'again'?")
                    npc<Neutral>("Nuffin'...")
                }
                3 -> {
                    npc<Neutral>("You know, boss, I don't think you're totally into this whole 'evil' thing.")
                    player<Happy>("I wonder what gave you that impression?")
                    npc<Neutral>("Well, I worked with a lot of evil people; some of the best.")
                    player<Happy>("Such as?")
                    npc<Neutral>("I'm not telling! I've got my principles to uphold.")
                    player<Happy>("There is honour amongst thieves, it would seem.")
                }
            }
        }
    }
}
