package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class FireTitan : Script {
    init {
        npcOperate("Interact", "fire_titan_familiar") {
            if (inventory.contains("tinderbox")) {
                npc<Neutral>("Relight my fire.")
                npc<Neutral>("A tinderbox is my only desire.")
                player<Happy>("What are you singing?")
                npc<Neutral>("Just a song I heard a while ago.")
                player<Happy>("A tinderbox is my only desire.")
                npc<Neutral>("You're just jealous of my singing voice.")
                player<Happy>("Where did you hear this again?")
                npc<Neutral>("Oh, you know, just with some other fire titans. Out for a night on the pyres.")
                player<Happy>("Hmm. Come on then. We have stuff to do.")
                return@npcOperate
            }
            when (random.nextInt(5)) {
                0 -> {
                    npc<Neutral>("Pick flax.")
                    npc<Neutral>("Jump to it.")
                    npc<Neutral>("If you want to get to Fletching level 99.")
                    player<Happy>("That song...is terrible.")
                    npc<Neutral>("Sorry.")
                }
                1 -> {
                    npc<Neutral>("You're fanning my flame with your wind spells.")
                    npc<Neutral>("I'm singeing the curtains with my heat.")
                    player<Happy>("Oooh, very mellow.")
                }
                2 -> {
                    npc<Neutral>("I'm burning up.")
                    npc<Neutral>("I want the world to know.")
                    npc<Neutral>("I got to let it show.")
                    player<Happy>("Catchy.")
                }
                3 -> {
                    npc<Neutral>("It's raining flame!")
                    npc<Neutral>("Huzzah!")
                    player<Happy>("You have a...powerful voice.")
                    npc<Neutral>("Thanks")
                }
                4 -> {
                    npc<Neutral>("Let's go fireside.")
                    npc<Neutral>("I think I've roasted the sofa.")
                    npc<Neutral>("I think I've burnt down the hall.")
                    player<Happy>("Can't you sing quietly?")
                    npc<Neutral>("Sorry.")
                }
            }
        }
    }
}
