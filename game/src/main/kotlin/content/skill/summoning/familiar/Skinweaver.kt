package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class Skinweaver : Script {
    init {
        npcOperate("Interact", "*_skinweaver_familiar") {
            if (!this["talked_to_skinweaver", false]) {
                this["talked_to_skinweaver"] = true
                npc<Frustrated>("This is my first time on this plane, naabe. I hope I can serve you well.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("What is your name? My name is $name, by the way.")
                    npc<Frustrated>("I get confused with these titles you humans take pride in. Fremennik names, first names, last names... Gorajo have no need to be individual - to have a name that no-one else has.")
                    player<Happy>("How can you not have a name?")
                    npc<Frustrated>("Everything we do is for the clan. If a problem arises, a role will be required, not an individual. The individual has no place among the gorajo.")
                    npc<Frustrated>("Please do not take it as rudeness, but I cannot understand how your world functions with names and individuals, naabe.")
                }
                1 -> {
                    player<Happy>("What does a skinweaver do, exactly?")
                    npc<Frustrated>("We are healers of livestock, crops and other gorajo. Which reminds me, naabe, do you mind if I ask you a question?")
                    player<Happy>("Sure.")
                    npc<Frustrated>("Do you find that human organs feel like slippery fish? And that your skin is stretchy like the dried sap of an utuku?")
                    player<Happy>("Uh. I'm feeling a little faint.")
                    npc<Frustrated>("Don't worry! I have cat spittle for your head should you fall. And I am on hand to suck any blood clots from your brain.")
                }
                2 -> {
                    player<Happy>("How do you like it in Daemonheim?.")
                    npc<Frustrated>("Naabe, let me tell you something. When I was a few years younger than I am now, I helped to heal a nustukh: a creature as big as three floors of this place.")
                    npc<Frustrated>("The nustukh do not benefit the gorajo in any way, but they are the reincarnations of our greatest leaders. They have great significance to our people.")
                    npc<Frustrated>("This nustukh was ravaged by a corruption that ate at every one of its organs. A skinweaver was required to crawl in through an open lesion and heal it: I volunteered immediately. I spent two weeks inside.")
                    npc<Frustrated>("I cannot help but be reminded of the nustukh in Daemonheim. The dungeons are as rank and unwholesome, and I feel that my powers are just as ineffective inside.")
                }
                3 -> {
                    player<Happy>("I don't have any more questions.")
                    npc<Frustrated>("I can understand your curiosity, naabe. Feel free to talk whenever you need.")
                }
            }
        }
    }
}
