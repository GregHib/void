package content.skill.summoning.familiar

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class AbyssalParasite : Script {
    init {
        npcOperate("Interact", "abyssal_parasite_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("*Stomach-turning slurping noises*<br>(Tdsa tukk!)")
                    player<Drunk>("Oh, the noises again.")
                    npc<Angry>("*Unpleasant slurpings*<br>(Hem s'htee?)")
                    player<Shock>("Please, just stop talking!")
                }
                1 -> {
                    npc<Neutral>("*Obscene gurglings*<br>(Ace'e e ur'y!)")
                    player<Shock>("I think I'm going to be sick... The noises! Oh, the terrifying noises.")
                }
                2 -> {
                    npc<Angry>("*Slobberings and slurpings*<br>(Ongk n'hd?)")
                    player<Drunk>("Oh, I'm not feeling so well.")
                    npc<Angry>("*Teeth-vibrating hisses and liquid slaverings*<br>(Uge f't es?)")
                    player<Drunk>("Please, have mercy!")
                    npc<Angry>("*Noises akin to a clogged drain being plunged*<br>(F'tp ohl't?)")
                    player<Drunk>("I shouldn't have eaten that kebab. Please stop talking!")
                }
                3 -> {
                    npc<Sad>("*Sounds best left undescribed*<br>(Noslr'rh...)")
                    player<Sad>("What's the matter?")
                    npc<Sad>("*More sounds best left undescribed*<br>(Kdso seo...)")
                    player<Quiz>("Could you...could you mime what the problem is?")
                    npc<Sad>("*Slighly lounder noises that are best left undescribed*<br>(Yiao itl!)")
                    player<Sad>("I want to help it but, aside from the language gap, it's noises make me retch!")
                }
            }
        }
    }
}
