package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class IronTitan : Script {
    init {
        npcOperate("Interact", "iron_titan_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    player<Quiz>("Titan?")
                    npc<Neutral>("Yes, boss?")
                    player<Quiz>("What's that in your hand?")
                    npc<Neutral>("I'm glad you asked that, boss.")
                    npc<Neutral>("This is the prototype for the Iron Titan (tm) action figure. You just pull this string here and he fights crime with real action sounds.")
                    player<Quiz>("Titan?")
                    npc<Neutral>("Yes, boss?")
                    player<Neutral>("Never mind.")
                }
                1 -> {
                    npc<Neutral>("Boss!")
                    player<Quiz>("What?")
                    npc<Neutral>("I've just had a vision of the future.")
                    player<Happy>("I didn't know you were a fortune teller. Let's hear it then.")
                    npc<Neutral>("Just imagine, boss, an Iron Titan (tm) on every desk.")
                    player<Neutral>("That doesn't even make sense.")
                    npc<Neutral>("Hmm. It was a bit blurry, perhaps the future is having technical issues at the moment.")
                    player<Neutral>("Riiight.")
                }
                2 -> {
                    player<Quiz>("Boss?")
                    npc<Neutral>("Yes, titan?")
                    player<Quiz>("You know how you're the boss and I'm the titan?")
                    npc<Neutral>("Yes?")
                    player<Quiz>("Do you think we could swap for a bit?")
                    npc<Neutral>("No, titan!")
                    player<Neutral>("Aww...")
                }
                3 -> {
                    player<Quiz>("How are you today, titan?")
                    npc<Neutral>("I'm very happy.")
                    player<Quiz>("That's marvellous, why are you so happy?")
                    npc<Neutral>("Because I love the great taste of Iron Titan (tm) cereal!")
                    player<Quiz>("?")
                    player<Neutral>("You're supposed to be working for me, not promoting yourself.")
                    npc<Neutral>("Sorry, boss.")
                }
            }
        }
    }
}
