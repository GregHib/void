package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SpiritScorpion : Script {
    init {
        npcOperate("Interact", "spirit_scorpion_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Hey, boss, how about we go to the bank?")
                    player<Happy>("And do what?")
                    npc<Neutral>("Well, we could open by shouting, 'Stand and deliver!'")
                    player<Happy>("Why does everything with you end with something getting held up?")
                    npc<Neutral>("That isn't true! Give me one example.")
                    player<Happy>("How about the post office?")
                    npc<Neutral>("How about another?")
                    player<Happy>("Those junior White Knights? The ones selling the gnome crunchies?")
                    npc<Neutral>("That was self defence.")
                    player<Happy>("No! No more hold-ups, stick-ups, thefts, or heists, you got that?")
                }
                1 -> {
                    player<Happy>("Say hello to my little friend!")
                    npc<Neutral>("What?")
                    player<Happy>("My little friend: you ignored him last time you met him.")
                    npc<Neutral>("So, who is your friend?")
                    player<Happy>("If I tell you, what is the point?")
                }
                2 -> {
                    npc<Neutral>("Hey, boss, I've been thinking.")
                    player<Happy>("That's never a good sign.")
                    npc<Neutral>("See, I heard about this railway...")
                    player<Happy>("We are not robbing it!")
                    npc<Neutral>("I might not have wanted to suggest that, boss...")
                    player<Happy>("Then what were you going to suggest?")
                    npc<Neutral>("That isn't important right now.")
                    player<Happy>("I thought as much.")
                }
                3 -> {
                    npc<Neutral>("Why do we never go to crossroads and rob travelers?")
                    player<Happy>("There are already highwaymen at the good spots.")
                    npc<Neutral>("Maybe we need to think bigger.")
                }
            }
        }
    }
}
