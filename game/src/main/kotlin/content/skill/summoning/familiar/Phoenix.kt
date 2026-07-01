package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Phoenix : Script {
    init {
        npcOperate("Interact", "phoenix_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Skreee skree skrooo skrooooouuu. (I want to burn something.)")
                    player<Happy>("Why are you looking at me like that?")
                    npc<Neutral>("Skeeeeooouoou! Skree skrooo, skrooouuee skreee! (Please! It won't hurt that much, and I'll bring you back straight away!)")
                    player<Happy>("Maybe later. Much later. When I'm dead from natural causes already. And medicine has failed to bring me back.")
                    npc<Neutral>("Skreee skreeeooouu skroou! (I'll hold you to it!)")
                }
                1 -> {
                    npc<Neutral>("May I ask you a question?")
                    player<Happy>("Skreeoooouuu, skreeee skreeeeoooo. (Yes, but you have already asked me a question.)")
                    player<Happy>("Skreeeooo, skreee skreeeeee skreeoooo. (You should have said 'May I ask you two questions?'.)")
                    npc<Neutral>("Erm, may I ask you two questions?")
                    player<Happy>("Skroo. (No.)")
                    npc<Neutral>("...")
                }
                2 -> {
                    player<Happy>("May I ask you... TWO questions?")
                    npc<Neutral>("Skree ree ree! Skree, skreee skrooou skreeeoou. (Heh heh heh. The answer to your first is yes. You may ask your second.)")
                    player<Happy>("What was RuneScape like in the distant past?")
                    npc<Neutral>("Skreee skreeeeout skreeou. Skreee skree. (It was like it is now, only younger.)")
                    player<Happy>("...")
                    player<Happy>("You, madam, are the most pestiferous poultry I have ever met.")
                    npc<Neutral>("Skree ree ree! (Heh heh heh!)")
                }
                3 -> {
                    player<Happy>("Skreeee, skree skrooo. Skrooooou skreee!")
                    npc<Neutral>("Skreee skroooue, skreeee skreeeeeeeou. (Either you need to practice your phoenixspeak, or I should burn you where you stand.)")
                    player<Happy>("So that didn't mean 'How are you feeling today?'")
                    npc<Neutral>("Skroo. Skroo, skreee skreou. (No, it didn't.")
                }
            }
        }
    }
}
