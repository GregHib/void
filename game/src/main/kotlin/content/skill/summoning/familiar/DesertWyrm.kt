package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class DesertWyrm : Script {
    init {
        npcOperate("Interact", "desert_wyrm_familiar") {
            if (equipped(EquipSlot.Weapon).id.endsWith("pickaxe")) {
                npc<Neutral>("If you have that pick, why make me dig?")
                player<Happy>("Because it's a little quicker and easier on my arms.")
                npc<Neutral>("I should take industrial action over this...")
                player<Happy>("You mean you won't work for me any more?")
                npc<Neutral>("No. It means me and the lads feed you legs-first into some industrial machinery, maybe the Blast Furnace.")
                player<Happy>("I'll just be over here, digging.")
                npc<Neutral>("That's the spirit, lad!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("This is so unsafe...I should have a hard hat for this work...")
                    player<Happy>("Well, I could get you a rune helm if you like - those are pretty hard.")
                    npc<Neutral>("Keep that up and you'll have the union on your back!")
                }
                1 -> {
                    npc<Neutral>("You can't touch me, I'm part of the union!")
                    player<Happy>("Is that some official \"no-touching\" policy or something?")
                    npc<Neutral>("You really don't get it, do you $name?")
                }
                2 -> {
                    npc<Neutral>("You know, you might want to register with the union.")
                    player<Happy>("What are the benefits?")
                    npc<Neutral>("I stop bugging you to join the union.")
                    player<Happy>("Ask that again later; I'll have to consider that generous proposal.")
                }
                3 -> {
                    npc<Neutral>("Why are you ignoring that good ore seam, mister?")
                    player<Happy>("Which ore seam?")
                    npc<Neutral>("There's a good ore seam right underneath us at this very moment.")
                    player<Happy>("Great! How long will it take for you to get to it?")
                    npc<Neutral>("Five years, give or take.")
                    player<Happy>("Five years!")
                    npc<Neutral>("That's if we go opencast, mind. I could probably reach it in three if I just dug.")
                    player<Happy>("Right. I see. I think I'll skip it thanks.")
                }
            }
        }
    }
}
