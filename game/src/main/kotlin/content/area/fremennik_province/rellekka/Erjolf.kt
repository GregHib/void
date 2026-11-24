package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Erjolf : Script {
    init {
        npcOperate("Talk-to", "erjolf") {
            npc<Happy>("Incredible! This'll show them!")
            player<Quiz>("Hello?")
            npc<Happy>("Oh! Hello there.")
            player<Quiz>("You seem rather excited about something.")
            npc<Shifty>("Who, me? No, I'm not excited about anything. I certainly haven't found anything amazing inside the caves here.")
            player<Quiz>("You won't mind if I have a look inside the cave, then.")
            npc<Angry>("Just you hold on a minute. I know what you adventurers are like, you'll wander in there and decide that anything you find is yours to take.")
            // TODO Tale of the Muspah
        }

        // https://runescape.wiki/w/Transcript:The_Tale_of_the_Muspah#Erjolf_2
        npcOperate("Talk-to", "erjolf_2") {
            player<Quiz>("So, do you think the Muspah's tail is a good enough trophy?")
            npc<Happy>("It's the best! I can't wait to show this off, I'll pass the Fremennik Trials, for sure!")
            player<Happy>("Glad I could help.")
            npc<Shifty>("Oh, yeah, about that...")
            npc<Shifty>("It'd probably be best to keep this on the quiet, yeah? Otherwise I might not get all the credit I deserve.")
            player<Angry>("You want me to lie about helping you?")
            npc<Shifty>("No, no, not at all! Just...don't mention it to anyone.")
            player<Neutral>("I'm making no promises.")
            npc<Neutral>("Fair enough. I thought it might be a bit much to ask. Well, see you around!")
            player<Neutral>("Bye.")
        }
    }
}
