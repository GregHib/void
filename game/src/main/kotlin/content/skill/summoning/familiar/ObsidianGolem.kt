package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class ObsidianGolem : Script {
    init {
        npcOperate("Interact", "obsidian_golem_familiar") {
            if (inventory.contains("fire_cape") || equipment.contains("fire_cape")) {
                npc<Neutral>("Truly, you are a powerful warrior, Master!")
                player<Happy>("I'm pleased you think so.")
                npc<Neutral>("It is my duty to respect you, Master.")
                player<Happy>("Oh, So you're just saying that to make me happy...")
                npc<Neutral>("I obey all orders, Master.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Let us go forth and prove our strength, Master!")
                    player<Happy>("Where would you like to prove it?")
                    npc<Neutral>("The caves of the TzHaar are filled with monsters for us to defeat, Master! TzTok-Jad shall quake in his slippers!")
                    player<Happy>("Have you ever met TzTok-Jad?")
                    npc<Neutral>("Alas, Master, I have not. No Master has ever taken me to see him.")
                }
                1 -> {
                    npc<Neutral>("How many foes have you defeated, Master?")
                    player<Happy>("Quite a few, I should think.")
                    npc<Neutral>("Was your first foe as mighty as the volcano, Master?")
                    player<Happy>("Um, not quite.")
                    npc<Neutral>("I am sure it must have been a deadly opponent, Master!")
                    player<Happy>("*Cough* It might have been a chicken. *Cough*")
                }
                2 -> {
                    npc<Neutral>("Master! We are truly a mighty duo!")
                    player<Happy>("Do you think so?")
                    npc<Neutral>("Of course, Master! I am programmed to believe so.")
                    player<Happy>("Do you do anything you're not programmed to?")
                    npc<Neutral>("No, Master.")
                    player<Happy>("I guess that makes things simple for you...")
                }
                3 -> {
                    npc<Neutral>("Do you ever doubt your programming, Master?")
                    player<Happy>("I don't have programming. I can think about whatever I like.")
                    npc<Neutral>("What do you think about, Master?")
                    player<Happy>("Oh, simple things: the sound of one hand clapping, where the gods come from...Simple things.")
                    npc<Neutral>("Paradox check = positive. Error. Reboot.")
                }
            }
        }
    }
}
